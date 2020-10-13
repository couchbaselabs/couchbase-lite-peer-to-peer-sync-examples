//
// Copyright (c) 2020 Couchbase, Inc All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.couchbase.android.listsync.db;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.couchbase.android.listsync.model.Produce;
import com.couchbase.android.listsync.util.FileUtils;
import com.couchbase.lite.Array;
import com.couchbase.lite.Blob;
import com.couchbase.lite.ConsoleLogger;
import com.couchbase.lite.CouchbaseLite;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.EncryptionKey;
import com.couchbase.lite.Expression;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.LogDomain;
import com.couchbase.lite.LogLevel;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;


@Singleton
public final class DatabaseManager {
    private static final String TAG = "DB";

    private static final String PROTO_DB_NAME = "userdb";
    private static final String DB_SUFFIX = ".cblite2";
    private static final String DB_FILE = PROTO_DB_NAME + DB_SUFFIX;
    private static final String DB_ASSET = DB_FILE + ".zip";
    private static final String TMP_DIR = "couchbase";

    private static final String DOC_ID = "doc::list";

    private static final String PROP_ITEMS = "items";

    private static final String PROP_KEY = "key";
    private static final String PROP_IMAGE = "image";
    private static final String PROP_VALUE = "value";

    @NonNull
    private final Executor dbExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    private final Scheduler dbScheduler = Schedulers.from(dbExecutor);

    // Note:
    // The database is now used from multiple threads: access to it must be synchronized.
    @GuardedBy("this")
    @Nullable
    private Database database;

    @Inject
    public DatabaseManager(@NonNull final Context ctxt) {
        CouchbaseLite.init(ctxt);

        final ConsoleLogger logger = Database.log.getConsole();
        logger.setLevel(LogLevel.DEBUG);
        logger.setDomains(LogDomain.ALL_DOMAINS);

        // make sure the proto-db is in place
        DatabaseConfiguration config = new DatabaseConfiguration();
        if (Database.exists(PROTO_DB_NAME, new File(config.getDirectory()))) { return; }

        File tmpDir = ctxt.getExternalFilesDir(TMP_DIR);
        if (tmpDir == null) { throw new IllegalStateException("Error creating temp dir"); }

        try (InputStream in = ctxt.getAssets().open(DB_ASSET)) { FileUtils.unzipToDir(in, tmpDir); }
        catch (IOException e) { throw new IllegalStateException("Failed unzipping db asset", e); }

        File dbFile = new File(tmpDir, DB_FILE);
        try { Database.copy(dbFile, PROTO_DB_NAME, config); }
        catch (CouchbaseLiteException e) { throw new IllegalStateException("Failed copying db asset", e); }

        FileUtils.erase(dbFile);
    }

    public boolean isLoggedIn() { return getDb() != null; }

    public Completable openDb(@NonNull String user, @NonNull String pwd) {
        return Completable
            .fromAction(() -> openDbAsync(user, pwd))
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable closeDb() {
        return Completable
            .fromAction(this::closeDbAsync)
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Produce>> getInSeason() {
        final Query query = QueryBuilder.select(SelectResult.property(PROP_ITEMS))
            .from(DataSource.database(getDb()))
            .where((Meta.id).equalTo(Expression.string(DOC_ID)));

        return Observable.<List<Produce>>create(emitter -> {
            final ListenerToken token = query.addChangeListener(
                dbExecutor,
                change -> {
                    final Throwable error = change.getError();
                    if (error != null) { emitter.onError(error); }
                    else { emitter.onNext(toProduce(change.getResults())); }
                });
            emitter.setDisposable(new Disposable() {
                private volatile boolean disposed;

                @Override
                public void dispose() {
                    disposed = true;
                    query.removeChangeListener(token);
                }

                @Override
                public boolean isDisposed() { return disposed; }
            });
        })
            .subscribeOn(dbScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    private void openDbAsync(@NonNull String user, @NonNull String pwd)
        throws CouchbaseLiteException {

        final DatabaseConfiguration config = new DatabaseConfiguration();
        final EncryptionKey encryptionKey = new EncryptionKey(pwd);

        final File dbDir = new File(config.getDirectory());

        final Database db;
        if (Database.exists(user, dbDir)) {
            config.setEncryptionKey(encryptionKey);
            db = new Database(user, config);
        }
        else {
            Database.copy(new File(dbDir, DB_FILE), user, config);
            db = new Database(user, config);
            db.changeEncryptionKey(encryptionKey);
        }

        synchronized (this) { database = db; }
    }

    private void closeDbAsync() throws CouchbaseLiteException {
        final Database db;
        synchronized (this) {
            db = database;
            database = null;
        }

        if (db != null) { db.close(); }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Produce> toProduce(@Nullable ResultSet resultSet) {
        final List<Produce> produce = new ArrayList<>();

        if (resultSet == null) { return produce; }

        final Result result = resultSet.next();
        if (result == null) { return produce; }

        final Array items = result.getArray(PROP_ITEMS);
        if (items == null) { return produce; }

        List rawProduce = items.toList();
        for (Map<String, ?> veg: (List<Map<String, ?>>) rawProduce) { produce.add(toProduce(veg)); }

        return produce;
    }

    private Produce toProduce(Map<String, ?> rawProduce) {
        final String name = (String) rawProduce.get(PROP_KEY);
        if (TextUtils.isEmpty(name)) { throw new IllegalStateException("Empty name (key) in DB"); }
        final Long done = (Long) rawProduce.get(PROP_VALUE);
        return new Produce(name, (Blob) rawProduce.get(PROP_IMAGE), (done == null) ? 0 : done);
    }

    private Database getDb() {
        synchronized (this) { return database; }
    }
}


