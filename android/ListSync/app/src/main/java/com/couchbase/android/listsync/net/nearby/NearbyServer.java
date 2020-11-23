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
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import com.couchbase.android.listsync.db.Db;


public final class NearbyServer extends BaseNearby {
    private static final String TAG = "NEARBY_SERVER";

    class NearbyCallback extends ConnectionLifecycleCallback {
        // Always automatically accept the connection.
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo info) {
            Log.d(TAG, "Connection initiated @" + endpointId + ": " + info.getEndpointName());
            Nearby.getConnectionsClient(ctxt).acceptConnection(endpointId, IGNORE_PAYLOADS);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            final int code = result.getStatus().getStatusCode();
            Log.d(TAG, "Connection result @" + endpointId + ": " + code);
            if (code != ConnectionsStatusCodes.STATUS_OK) { return; }
            connectEndpoint(endpointId);
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d(TAG, "Disconnected: " + endpointId);
            disconnectEndpoint(endpointId);
        }
    }

    private static final PayloadCallback IGNORE_PAYLOADS = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload ignore) { }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate ignore) { }
    };


    @NonNull
    final Set<String> connections = new HashSet<>();

    @NonNull
    private final NearbyCallback callback;

    @NonNull
    private Payload cachedPayload;

    @Inject
    public NearbyServer(@NonNull final Context ctxt, @NonNull Db db) {
        super(ctxt, db);
        this.callback = new NearbyCallback();
        cachedPayload = toPayload(null);
    }

    public void advertise(boolean advertise) {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);

        if (!advertise) {
            nearby.stopAdvertising();
            Log.i(TAG, "advertising started: " + user + "@" + pkgName);
            return;
        }

        nearby.startAdvertising(user, pkgName, callback, new AdvertisingOptions(Strategy.P2P_CLUSTER));
        Log.i(TAG, "Advertising stopped: " + user + "@" + pkgName);
    }

    @UiThread
    public void update(Set<URI> endpoints) {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);
        final List<URI> endpts = new ArrayList<>(endpoints);
        nearbyExecutor.execute(() -> {
            final Payload payload = toPayload(endpts);
            synchronized (this) { cachedPayload = payload; }
            for (String endpointId: connections) { nearby.sendPayload(endpointId, payload); }
        });
    }

    @UiThread
    void connectEndpoint(@NonNull String endpointId) {
        if (connections.contains(endpointId)) { return; }
        connections.add(endpointId);

        final Payload payload;
        synchronized (this) { payload = cachedPayload; }

        nearbyExecutor.execute(() -> Nearby.getConnectionsClient(ctxt).sendPayload(endpointId, payload));
    }

    @UiThread
    void disconnectEndpoint(@NonNull String endpointId) { connections.remove(endpointId); }
}


