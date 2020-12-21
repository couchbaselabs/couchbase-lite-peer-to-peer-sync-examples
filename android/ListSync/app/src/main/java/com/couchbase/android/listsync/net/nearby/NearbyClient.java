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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

import com.couchbase.android.listsync.db.Db;
import com.couchbase.android.listsync.model.Device;
import com.couchbase.android.listsync.model.Listener;


public class NearbyClient extends BaseNearby {
    private static final String TAG = "NEARBY_CLIENT";

    class NearbyDiscovery extends EndpointDiscoveryCallback {
        @NonNull
        private final Emitter<Collection<Device>> emitter;

        NearbyDiscovery(@NonNull Emitter<Collection<Device>> emitter) { this.emitter = emitter; }

        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            if (currentlyNearby.containsKey(endpointId)) { return; }
            final Device endpoint = new Device(endpointId, info.getEndpointName());
            Log.d(TAG, "Found endpoint: " + endpoint);
            currentlyNearby.put(endpointId, endpoint);
            emitter.onNext(currentlyNearby.values());
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            if (!currentlyNearby.containsKey(endpointId)) { return; }
            final Device endpoint = currentlyNearby.remove(endpointId);
            Log.d(TAG, "Lost endpoint: " + endpoint);
            emitter.onNext(currentlyNearby.values());
        }
    }

    class ConnectionHandler extends ConnectionLifecycleCallback {
        class ReceiveEndpointList extends PayloadCallback {
            @Override
            public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                if (payload.getType() != Payload.Type.BYTES) { return; }
                Log.d(TAG, "Received payload: " + payload.getId());
                receive(emitter, payload);
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate ignore) { }
        }

        @NonNull
        private final ObservableEmitter<Collection<Listener>> emitter;

        ConnectionHandler(@NonNull ObservableEmitter<Collection<Listener>> emitter) { this.emitter = emitter; }

        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo info) {
            Log.d(TAG, "Connection initiated @" + endpointId + ": " + info.getEndpointName());
            Nearby.getConnectionsClient(ctxt).acceptConnection(endpointId, new ReceiveEndpointList());
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            Log.d(TAG, "Connection result @" + endpointId + ": " + result.getStatus().getStatusCode());
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d(TAG, "Disconnected: " + endpointId);
        }
    }

    @NonNull
    private final Map<String, Device> currentlyNearby = new HashMap<>();

    @Inject
    public NearbyClient(@NonNull final Context ctxt, @NonNull Db db) { super(ctxt, db); }

    /**
     * I have no idea what will happen if you call this twice, even from different instances...
     */
    @UiThread
    public Observable<Collection<Device>> startDiscovery() {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);
        return Observable.create((emitter) -> {
            nearby.startDiscovery(pkgName, new NearbyDiscovery(emitter), new DiscoveryOptions(Strategy.P2P_CLUSTER));
            emitter.setDisposable(new Disposable() {
                boolean disposed;

                @Override
                public boolean isDisposed() { return disposed; }

                @Override
                public void dispose() {
                    disposed = true;
                    nearby.stopDiscovery();
                    Log.i(TAG, "Discovery stopped");
                }
            });
            Log.i(TAG, "Discovery started");
        });
    }

    @UiThread
    public Observable<Collection<Listener>> getListenersForDevice(@NonNull Device device) {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);
        final String deviceId = device.getId();
        return Observable.create((emitter) -> {
            nearby.requestConnection(user, deviceId, new ConnectionHandler(emitter));
            emitter.setDisposable(new Disposable() {
                boolean disposed;

                @Override
                public boolean isDisposed() { return disposed; }

                @Override
                public void dispose() {
                    disposed = true;
                    nearby.disconnectFromEndpoint(deviceId);
                    Log.i(TAG, "Disconnecting from device: " + deviceId);
                }
            });
            Log.i(TAG, "Connecting to device: " + deviceId);
        });
    }

    void receive(@NonNull ObservableEmitter<Collection<Listener>> emitter, @NonNull Payload payload) {
        final List<URI> uris = fromPayload(payload.asBytes());
        if (uris == null) { return; }
        final Set<Listener> listeners = new HashSet<>();
        for (URI uri: uris) { listeners.add(new Listener(uri)); }
        emitter.onNext(listeners);
    }
}
