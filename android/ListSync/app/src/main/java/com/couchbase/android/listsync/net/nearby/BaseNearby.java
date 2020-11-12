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
package com.couchbase.android.listsync.net.nearby;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.PayloadCallback;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


abstract class BaseNearby {
    class NearbyCallback extends ConnectionLifecycleCallback {
        // Always automatically accept the connection.
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo info) {
            Nearby.getConnectionsClient(ctxt).acceptConnection(endpointId, getPayloadCallback());
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().getStatusCode() != ConnectionsStatusCodes.STATUS_OK) { return; }
            if (connections.contains(endpointId)) { return; }
            connections.add(endpointId);
            newEndpoint(endpointId);
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) { connections.remove(endpointId); }
    }

    @NonNull
    protected final Executor nearbyExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    protected final Scheduler nearbyScheduler = Schedulers.from(nearbyExecutor);

    @NonNull
    protected final Set<String> connections = new HashSet<>();

    @NonNull
    protected final Context ctxt;
    @NonNull
    protected final String user;
    @NonNull
    protected final String pkgName;

    protected BaseNearby(@NonNull Context ctxt, @NonNull String user) {
        this.ctxt = ctxt.getApplicationContext();
        this.user = user;
        this.pkgName = ctxt.getPackageName();
    }

    @NonNull
    protected abstract PayloadCallback getPayloadCallback();

    protected abstract void newEndpoint(@NonNull String endpointId);
}
