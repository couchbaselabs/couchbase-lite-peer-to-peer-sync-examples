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
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

import com.couchbase.android.listsync.net.nearby.NearbyServer;
import com.couchbase.android.listsync.net.p2p.ListenerManager;


@Singleton
public class ServerViewModel extends ViewModel {
    private static final String TAG = "SERVER_VM";

    @NonNull
    private final MutableLiveData<Set<URI>> servers = new MutableLiveData<>();

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
    public LiveData<Set<URI>> getServers() {
        updateServers(serverMgr.getServers());
        return servers;
    }

    @NonNull
    public LiveData<Set<URI>> startServer() {
        disposables.add(serverMgr.startServer().subscribe(
            this::updateServers,
            e -> Log.w(TAG, "Failed to start server", e)));
        return servers;
    }

    @NonNull
    public LiveData<Set<URI>> stopServer(@NonNull URI uri) {
        disposables.add(serverMgr.stopServer(uri).subscribe(
            this::updateServers,
            e -> Log.w(TAG, "Failed to stop server", e)));
        return servers;
    }

    // continue to advertise...
    public void cancel() { disposables.clear(); }

    private void updateServers(Set<URI> newServers) {
        final Set<URI> curServers = servers.getValue();
        final int curServerCount = (curServers == null) ? 0 : curServers.size();

        Log.d(TAG, "Update servers: " + newServers.size() + ", " + curServerCount);

        if (curServerCount == 0) {
            if (newServers.isEmpty()) { return; }
            nearbyMgr.advertise(true);
        }
        else if (newServers.isEmpty()) {
            nearbyMgr.advertise(false);
        }

        servers.setValue(newServers);

        nearbyMgr.update(newServers);
    }
}
