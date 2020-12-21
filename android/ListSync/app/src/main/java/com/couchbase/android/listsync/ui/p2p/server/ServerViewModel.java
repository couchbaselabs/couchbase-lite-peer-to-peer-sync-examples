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
package com.couchbase.android.listsync.ui.p2p.server;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.net.URI;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

import com.couchbase.android.listsync.net.nearby.NearbyServer;
import com.couchbase.android.listsync.net.p2p.ListenerManager;


@Singleton
public class ServerViewModel extends ViewModel {
    private static final String TAG = "SERVER_VM";

    @NonNull
    private final MutableLiveData<URI> server = new MutableLiveData<>();

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final ListenerManager serverMgr;
    @NonNull
    private final NearbyServer nearbyMgr;

    @Inject
    public ServerViewModel(@NonNull ListenerManager serverMgr, @NonNull NearbyServer nearbyMgr) {
        this.serverMgr = serverMgr;
        this.nearbyMgr = nearbyMgr;
    }

    @NonNull
    public LiveData<URI> getServers() {
        updateServers(serverMgr.getServers());
        return server;
    }

    @NonNull
    public LiveData<URI> startServer() {
        disposables.add(serverMgr.startServer().subscribe(
            this::updateServers,
            e -> Log.w(TAG, "Failed to start server", e)));
        return server;
    }

    @NonNull
    public LiveData<URI> stopServer() {
        final URI uri = server.getValue();
        if (uri != null) {
            disposables.add(serverMgr.stopServer(uri).subscribe(
                this::updateServers,
                e -> Log.w(TAG, "Failed to stop server", e)));
        }
        return server;
    }

    // Note: continue to advertise...
    public void cancel() { disposables.clear(); }

    private void updateServers(Collection<URI> newServers) {
        final URI newServer = (newServers.isEmpty()) ? null : newServers.iterator().next();
        final URI curServer = server.getValue();

        Log.d(TAG, "Update server: " + curServer + " => " + newServer);

        if (curServer == null) {
            if (newServer != null) { nearbyMgr.advertise(true); }
        }
        else if (newServers.isEmpty()) { nearbyMgr.advertise(false); }

        server.setValue(newServer);

        nearbyMgr.update(newServers);

        cancel();
    }
}
