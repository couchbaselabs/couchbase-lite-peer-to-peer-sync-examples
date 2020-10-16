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

import com.couchbase.android.listsync.p2p.SyncManager;


@Singleton
public class ServerViewModel extends ViewModel {
    private static final String TAG = "SRV_VM";

    @NonNull
    private final MutableLiveData<Set<URI>> servers = new MutableLiveData<>();

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final SyncManager sync;

    @Inject
    public ServerViewModel(@NonNull SyncManager sync) { this.sync = sync; }

    @NonNull
    public LiveData<Set<URI>> getServers() {
        servers.setValue(sync.getServers());
        return servers;
    }

    @NonNull
    public LiveData<Set<URI>> startServer() {
        disposables.add(sync.startServer().subscribe(
            servers::setValue,
            e -> Log.w(TAG, "Failed to start server", e)));
        return servers;
    }

    @NonNull
    public LiveData<Set<URI>> stopServer(@NonNull URI uri) {
        disposables.add(sync.stopServer(uri).subscribe(
            servers::setValue,
            e -> Log.w(TAG, "Failed to stop server", e)));
        return servers;
    }

    public void cancel() { disposables.clear(); }
}
