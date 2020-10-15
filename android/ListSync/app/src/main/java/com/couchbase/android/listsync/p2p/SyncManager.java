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
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.couchbase.android.listsync.db.DatabaseManager;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.URLEndpointListener;


@Singleton
public final class SyncManager {
    private static final String TAG = "SYNC";

    @NonNull
    private final Scheduler syncScheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    @NonNull
    private final DatabaseManager dbMgr;

    // Accessed *ONLY* on the syncScheduler thread
    private URLEndpointListener listener;

    @Inject
    public SyncManager(@NonNull DatabaseManager dbMgr) { this.dbMgr = dbMgr; }

    public Observable<String> startListener() {
        return Observable
            .fromCallable(this::startListenerAsync)
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable stopListener() {
        return Completable
            .fromAction(this::stopListenerAsync)
            .subscribeOn(syncScheduler)
            .observeOn(AndroidSchedulers.mainThread());
    }

    @WorkerThread
    public String startListenerAsync() throws CouchbaseLiteException {
        if (listener != null) { throw new IllegalStateException("listener already running"); }

        listener = new URLEndpointListener(dbMgr.getListenerConfig());

        listener.start();

        final String uri = listener.getUrls().get(0).toString();
        Log.d(TAG, "listener started at: " + uri);

        return uri;
    }


    @WorkerThread
    public void stopListenerAsync() throws CouchbaseLiteException {
        if (listener == null) { throw new IllegalStateException("no listener running"); }
        final String uri = listener.getUrls().get(0).toString();
        listener.stop();
        Log.d(TAG, "listener stopped at: " + uri);
    }
}
