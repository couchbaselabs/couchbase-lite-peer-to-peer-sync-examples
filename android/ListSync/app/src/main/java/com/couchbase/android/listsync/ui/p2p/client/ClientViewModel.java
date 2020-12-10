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
package com.couchbase.android.listsync.ui.p2p.client;

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

import com.couchbase.android.listsync.model.Client;
import com.couchbase.android.listsync.net.p2p.ReplicatorManager;


@Singleton
public class ClientViewModel extends ViewModel {
    private static final String TAG = "CLIENT_VM";

    public static final String SCHEME_WSS = "wss";


    @NonNull
    private final MutableLiveData<Set<Client>> clients = new MutableLiveData<>();

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final ReplicatorManager replMgr;

    @Inject
    public ClientViewModel(@NonNull ReplicatorManager replMgr) { this.replMgr = replMgr; }

    @NonNull
    public LiveData<Set<Client>> getClients() {
        disposables.add(replMgr.observeClients().subscribe(clients::setValue));
        return clients;
    }

    public void startClient(@NonNull URI uri) {
        disposables.add(replMgr.startClient(uri).subscribe(
            () -> Log.i(TAG, "Client started @" + uri),
            e -> Log.w(TAG, "Failed to start client @" + uri, e)));
    }

    public void restartClient(@NonNull Client client) {
        disposables.add(replMgr.restartClient(client).subscribe(
            () -> Log.i(TAG, "Client restarted @" + client),
            e -> Log.w(TAG, "Failed to restart client @" + client, e)));
    }

    public void stopClient(@NonNull Client client) {
        disposables.add(replMgr.stopClient(client).subscribe(
            () -> Log.i(TAG, "Client stopped @" + client),
            e -> Log.w(TAG, "Failed to stop client @" + client, e)));
    }

    public void deleteClient(@NonNull Client client) {
        disposables.add(replMgr.deleteClient(client).subscribe(
            () -> Log.i(TAG, "Client deleted @" + client),
            e -> Log.w(TAG, "Failed to delete client @" + client, e)));
    }

    public void cancel() { disposables.clear(); }
}
