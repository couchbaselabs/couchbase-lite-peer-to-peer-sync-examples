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
package com.couchbase.android.listsync.net.p2p;

import android.util.Log;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import java.net.URI;
import java.util.Set;
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

import com.couchbase.android.listsync.db.Db;
import com.couchbase.android.listsync.model.Client;
import com.couchbase.android.listsync.util.ObservableMap;
import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;


@Singleton
public final class ReplicatorManager {
    private static final String TAG = "REPLICATORS";

    @NonNull
    private final Executor syncExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    private final Scheduler syncScheduler = Schedulers.from(syncExecutor);

    @NonNull
    @GuardedBy("replicators")
    private final ObservableMap<Client, Replicator> replicators = new ObservableMap<>();

    @NonNull
    private final Db dbMgr;

    @Inject
    public ReplicatorManager(@NonNull Db dbMgr) { this.dbMgr = dbMgr; }

    @UiThread
    @NonNull
    public Completable startClient(@NonNull URI uri) {
        return Completable
            .fromRunnable(() -> startClientAsync(uri))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable restartClient(Client client) {
        return Completable
            .fromRunnable(() -> restartClientAsync(client))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @UiThread
    @NonNull
    public Completable stopClient(@NonNull Client client) {
        return Completable
            .fromRunnable(() -> stopClientAsync(client))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteClient(Client client) {
        return Completable
            .fromRunnable(() -> deleteClientAsync(client))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public Observable<Set<Client>> observeClients() {
        return Observable.<Set<Client>>create(emitter -> {
            replicators.addObserver(emitter);
            emitter.setDisposable(new Disposable() {
                @Override
                public void dispose() { replicators.removeObserver(emitter); }

                @Override
                public boolean isDisposed() { return replicators.isObservedBy(emitter); }
            });
        })
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    void onReplicatorStateChange(@NonNull URI uri, @NonNull Replicator repl, @NonNull ReplicatorChange change) {
        final Replicator.Status status = change.getStatus();

        final CouchbaseLiteException err = status.getError();
        if (err != null) { Log.w(TAG, "Replicator error: " + uri, err); }

        final AbstractReplicator.ActivityLevel state = status.getActivityLevel();
        Log.i(TAG, "Replicator in state " + state + ": " + uri);

        replicators.replace(new Client(uri, state), repl);
    }

    @WorkerThread
    private void startClientAsync(@NonNull URI uri) {
        replicators.put(new Client(uri, AbstractReplicator.ActivityLevel.STOPPED), null);

        final ReplicatorConfiguration config = dbMgr.getReplicatorConfig(new URLEndpoint(uri));
        config.setContinuous(true);
        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        config.setAcceptOnlySelfSignedServerCertificate(true);

        final Replicator replicator = new Replicator(config);
        replicator.addChangeListener(syncExecutor, change -> onReplicatorStateChange(uri, replicator, change));

        replicator.start(false);
    }

    private void restartClientAsync(Client client) {
        final Replicator replicator = replicators.get(client);
        if (replicator == null) {
            Log.w(TAG, "Attempt to restart non-existent replicator for: " + client);
            return;
        }

        Log.i(TAG, "Restarting replicator @" + replicator.getConfig() + " for " + client);
        replicator.start(false);
    }

    @WorkerThread
    private void stopClientAsync(@NonNull Client client) {
        final Replicator replicator = replicators.get(client);
        if (replicator == null) {
            Log.w(TAG, "Attempt to stop non-existent replicator for: " + client);
            return;
        }

        Log.i(TAG, "Stopping replicator @" + replicator.getConfig() + " for " + client);
        replicator.stop();
    }

    private void deleteClientAsync(Client client) {
        final Replicator replicator = replicators.remove(client);
        if (replicator == null) { return; }
        Log.i(TAG, "Deleted replicator  @" + replicator.getConfig() + " for " + client);
        replicator.stop();
    }
}
