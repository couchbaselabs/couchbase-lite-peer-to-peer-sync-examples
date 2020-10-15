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
package com.couchbase.android.listsync.ui.server;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

import com.couchbase.android.listsync.p2p.SyncManager;


@Singleton
public class ServerViewModel extends ViewModel {
    private static final String TAG = "SRV_VM";

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final MutableLiveData<String> endpoint = new MutableLiveData<>();

    @NonNull
    private final SyncManager sync;

    @Inject
    public ServerViewModel(@NonNull SyncManager sync) { this.sync = sync; }

    public LiveData<String> startListener() {
        disposables.add(sync.startListener().subscribe(endpoint::setValue));
        return endpoint;
    }

    public void stopListener() {
        sync.stopListener().subscribe(() -> {}, e -> Log.w(TAG, "failed to stop listener", e));
    }

    public void cancel() { disposables.clear(); }
}
