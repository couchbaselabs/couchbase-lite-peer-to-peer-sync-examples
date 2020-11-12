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
package com.couchbase.android.listsync.ui.p2p.nearby;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.Disposable;

import com.couchbase.android.listsync.db.DatabaseManager;
import com.couchbase.android.listsync.model.Endpoint;
import com.couchbase.android.listsync.net.nearby.NearbyClient;


@Singleton
public class NearbyViewModel extends ViewModel {
    private static final String TAG = "NEAR_VM";

    @NonNull
    private final MutableLiveData<Collection<Endpoint>> nearby = new MutableLiveData<>();

    @NonNull
    private final NearbyClient nearbyMgr;

    @Nullable
    private Disposable discoveryDisposable;

    @Inject
    public NearbyViewModel(@NonNull Context ctxt, @NonNull DatabaseManager db) {
        final String user = db.getUser();
        if (TextUtils.isEmpty(user)) { throw new IllegalStateException("Attempt to use nearby before sign in"); }
        this.nearbyMgr = new NearbyClient(ctxt, user);
    }

    @NonNull
    public LiveData<Collection<Endpoint>> getNearby() {
        if (discoveryDisposable == null) {
            discoveryDisposable = nearbyMgr.startDiscovery()
                .subscribe(
                    nearby::setValue,
                    e -> Log.w(TAG, "Discovery error", e));
        }
        return nearby;
    }

    public void selectNearby(Endpoint endpoint) {
        Log.d(TAG, "Selected: " + endpoint);
    }

    public void cancel() {
        if (discoveryDisposable != null) {
            discoveryDisposable.dispose();
            discoveryDisposable = null;
        }
    }
}
