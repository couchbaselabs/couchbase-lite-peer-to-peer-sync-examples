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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.nearby.Nearby;
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
import io.reactivex.disposables.Disposable;

import com.couchbase.android.listsync.model.Endpoint;


public class NearbyClient extends BaseNearby {
    private static final String TAG = "NEARBY_CLIENT";

    class NearbyDiscovery extends EndpointDiscoveryCallback {
        @NonNull
        private final Emitter<Collection<Endpoint>> emitter;

        public NearbyDiscovery(@NonNull Emitter<Collection<Endpoint>> emitter) { this.emitter = emitter; }

        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            if (currentlyNearby.containsKey(endpointId)) { return; }
            final Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
            Log.i(TAG, "Found endpoint: " + endpoint);
            currentlyNearby.put(endpointId, endpoint);
            emitter.onNext(currentlyNearby.values());
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            if (!currentlyNearby.containsKey(endpointId)) { return; }
            final Endpoint endpoint = currentlyNearby.remove(endpointId);
            Log.i(TAG, "Lost endpoint: " + endpoint);
            emitter.onNext(currentlyNearby.values());
        }
    }

    static class ReceiveEndpointList extends PayloadCallback {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            if (payload.getType() != Payload.Type.BYTES) { return; }
            receiveServerList(payload.asBytes());
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate ignore) { }

        private void receiveServerList(@NonNull byte[] rawEndpoints) {
            ObjectInputStream oStream = null;
            try {
                final ByteArrayInputStream bStream = new ByteArrayInputStream(rawEndpoints);
                oStream = new ObjectInputStream(bStream);
                final Object endpoints = oStream.readObject();
            }
            catch (IOException | ClassNotFoundException e) {
                Log.w(TAG, "Failed advertising servers", e);
            }
            finally {
                if (oStream != null) {
                    try { oStream.close(); } catch (IOException ignore) { }
                }
            }
        }
    }

    @NonNull
    private final Map<String, Endpoint> currentlyNearby = new HashMap<>();

    public NearbyClient(@NonNull final Context ctxt, @NonNull String user) { super(ctxt, user); }

    // I have no idea what will happen if you call this twice.
    @UiThread
    public Observable<Collection<Endpoint>> startDiscovery() {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);
        return Observable.create((emitter) -> {
            nearby.startDiscovery(
                this.pkgName,
                new NearbyDiscovery(emitter),
                new DiscoveryOptions(Strategy.P2P_CLUSTER));
            emitter.setDisposable(new Disposable() {
                boolean disposed = false;

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

    @NonNull
    @Override
    protected PayloadCallback getPayloadCallback() { return new ReceiveEndpointList(); }

    @Override
    protected void newEndpoint(@NonNull String endpointId) { }
}

