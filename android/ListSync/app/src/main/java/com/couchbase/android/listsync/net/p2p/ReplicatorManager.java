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
import com.couchbase.android.listsync.util.ObservableMap;
import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;


@Singleton
public final class ReplicatorManager {
    private static final String TAG = "SYNC";

    @NonNull
    private final Executor syncExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    private final Scheduler syncScheduler = Schedulers.from(syncExecutor);

    @NonNull
    @GuardedBy("replicators")
    private final ObservableMap<URI, Replicator> replicators = new ObservableMap<>();

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

    @NonNull
    public Observable<Set<URI>> observeClients() {
        return Observable.<Set<URI>>create(emitter -> {
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

    @UiThread
    @NonNull
    public Completable stopClient(@NonNull URI uri) {
        return Completable
            .fromRunnable(() -> stopClientAsync(uri))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @WorkerThread
    private void startClientAsync(@NonNull URI uri) {
        final ReplicatorConfiguration config = dbMgr.getReplicatorConfig(new URLEndpoint(uri));
        config.setContinuous(true);
        config.setReplicatorType(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL);
        config.setAcceptOnlySelfSignedServerCertificate(true);

        final Replicator replicator = new Replicator(config);

        replicator.addChangeListener(
            syncExecutor,
            change -> {
                if (change == null) { return; }
                final Replicator.Status status = change.getStatus();

                final CouchbaseLiteException err = status.getError();
                if (err != null) { Log.w(TAG, "Replicator error: " + uri, err); }

                final AbstractReplicator.ActivityLevel state = status.getActivityLevel();
                Log.i(TAG, "Replicator in state " + state + ": " + uri);
                if (state == null) { return; }
                switch (state) {
                    case CONNECTING:
                    case IDLE:
                    case BUSY:
                        replicators.put(uri, replicator);
                        break;

                    case STOPPED:
                    case OFFLINE:
                        replicators.remove(uri);
                        break;
                }
            });

        replicator.start(false);
    }

    @WorkerThread
    private void stopClientAsync(@NonNull URI uri) {
        final Replicator replicator = replicators.remove(uri);
        if (replicator == null) {
            Log.w(TAG, "Attempt to stop non-existent replicator: " + uri);
        }
        else {
            Log.i(TAG, "Stopping client @" + replicator.getConfig());
            replicator.stop();
        }
    }
}
