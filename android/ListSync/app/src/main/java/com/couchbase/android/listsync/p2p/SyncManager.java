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
package com.couchbase.android.listsync.p2p;

import android.util.Log;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.couchbase.android.listsync.db.DatabaseManager;
import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.couchbase.lite.URLEndpointListener;
import com.couchbase.lite.URLEndpointListenerConfiguration;


@Singleton
public final class SyncManager {
    private static final String TAG = "SYNC";

    static class ObservableMap<K, V> {
        private final Map<K, V> content = new HashMap<>();
        private final Set<ObservableEmitter<Set<K>>> observers = Collections.newSetFromMap(new WeakHashMap<>());

        public void addObserver(ObservableEmitter<Set<K>> observer) {
            synchronized (observers) { observers.add(observer); }
        }

        public boolean isObservedBy(ObservableEmitter<Set<K>> observer) {
            synchronized (observers) { return observers.contains(observer); }
        }

        public void removeObserver(ObservableEmitter<Set<K>> observer) {
            synchronized (observers) { observers.remove(observer); }
        }

        public void put(K key, V value) {
            final Set<K> keySet;
            synchronized (content) {
                content.put(key, value);
                keySet = content.keySet();
            }
            notifyObservers(keySet);
        }

        public V remove(K key) {
            final V value;
            final Set<K> keySet;
            synchronized (content) {
                value = content.remove(key);
                keySet = content.keySet();
            }
            notifyObservers(keySet);
            return value;
        }

        private void notifyObservers(@NonNull Set<K> keySet) {
            synchronized (observers) {
                for (ObservableEmitter<Set<K>> obs: observers) {
                    if (obs != null) { obs.onNext(keySet); }
                }
            }
        }
    }


    @NonNull
    private final Executor syncExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    private final Scheduler syncScheduler = Schedulers.from(syncExecutor);

    @NonNull
    @GuardedBy("listeners")
    private final Map<URI, URLEndpointListener> servers = new HashMap<>();

    @NonNull
    @GuardedBy("replicators")
    private final ObservableMap<URI, Replicator> clients = new ObservableMap<>();

    @NonNull
    private final DatabaseManager dbMgr;

    @Inject
    public SyncManager(@NonNull DatabaseManager dbMgr) { this.dbMgr = dbMgr; }

    @NonNull
    public Set<URI> getServers() {
        synchronized (servers) { return servers.keySet(); }
    }

    @NonNull
    public Observable<Set<URI>> observeClients() {
        return Observable.<Set<URI>>create(emitter -> {
            clients.addObserver(emitter);
            emitter.setDisposable(new Disposable() {
                @Override
                public void dispose() { clients.removeObserver(emitter); }

                @Override
                public boolean isDisposed() { return clients.isObservedBy(emitter); }
            });
        })
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @UiThread
    @NonNull
    public Observable<Set<URI>> startServer() { return startServer(null, 0); }

    @UiThread
    @NonNull
    public Observable<Set<URI>> startServer(@Nullable String iface, int port) {
        return Observable
            .fromCallable(() -> startServerAsync(iface, port))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @UiThread
    @NonNull
    public Observable<Set<URI>> stopServer(@NonNull URI uri) {
        return Observable
            .fromCallable(() -> stopServerAsync(uri))
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @UiThread
    @NonNull
    public Completable startClient(@NonNull URI uri) {
        return Completable
            .fromRunnable(() -> startClientAsync(uri))
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
    private Set<URI> startServerAsync(@Nullable String iface, int port) throws CouchbaseLiteException {
        final URLEndpointListenerConfiguration config = dbMgr.getListenerConfig();
        if (iface != null) { config.setNetworkInterface(iface); }
        if (port > URLEndpointListenerConfiguration.MIN_PORT) { config.setPort(port); }

        final URLEndpointListener listener = new URLEndpointListener(config);

        Log.i(TAG, "Starting server @" + config);
        listener.start();

        final URI uri = listener.getUrls().get(0);
        addServer(uri, listener);


        return getServers();
    }

    @WorkerThread
    public Set<URI> stopServerAsync(@NonNull URI uri) {
        final URLEndpointListener listener = removeServer(uri);
        if (listener == null) {
            Log.i(TAG, "Attempt to stop non-existent listener: " + uri);
        }
        else {
            Log.d(TAG, "Stopping server @" + listener.getConfig());
            listener.stop();
        }

        return getServers();
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
                        clients.put(uri, replicator);
                        break;

                    case STOPPED:
                    case OFFLINE:
                        clients.remove(uri);
                        break;
                }
            });

        replicator.start(false);
    }

    @WorkerThread
    private void stopClientAsync(@NonNull URI uri) {
        final Replicator replicator = clients.remove(uri);
        if (replicator == null) {
            Log.i(TAG, "Attempt to stop non-existent replicator: " + uri);
        }
        else {
            Log.d(TAG, "Stopping client @" + replicator.getConfig());
            replicator.stop();
        }
    }

    private void addServer(URI uri, URLEndpointListener listener) {
        synchronized (servers) { servers.put(uri, listener); }
    }

    @Nullable
    private URLEndpointListener removeServer(URI uri) {
        synchronized (servers) { return servers.remove(uri); }
    }
}
