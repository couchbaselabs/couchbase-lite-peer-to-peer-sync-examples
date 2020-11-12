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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;


public final class NearbyServer extends BaseNearby {
    private static final String TAG = "NEARBY_SERVER";

    private static class IgnorePayloads extends PayloadCallback {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload ignore) { }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate ignore) { }
    }


    @NonNull
    private final NearbyCallback callback;

    @NonNull
    private Payload data;

    public NearbyServer(@NonNull final Context ctxt, @NonNull String user, @NonNull Set<URI> endpoints) {
        super(ctxt, user);
        this.callback = new NearbyCallback();
        data = toPayload(endpoints);
    }

    public void advertise(boolean advertise) {
        final ConnectionsClient nearby = Nearby.getConnectionsClient(ctxt);

        if (!advertise) {
            nearby.stopAdvertising();
            Log.i(TAG, "Stop advertising " + user + "@" + pkgName);
            return;
        }

        nearby.startAdvertising(user, pkgName, callback, new AdvertisingOptions(Strategy.P2P_CLUSTER));
        Log.i(TAG, "Start advertising " + user + "@" + pkgName);
    }

    public void update(Set<URI> endpoints) {
        final Payload payload = toPayload(endpoints);
        for (String endpointId: connections) { nearbyExecutor.execute(() -> send(endpointId, payload)); }
    }

    @NonNull
    @Override
    protected PayloadCallback getPayloadCallback() { return new IgnorePayloads(); }

    @Override
    protected void newEndpoint(@NonNull String endpointId) {
        final Payload payload;
        synchronized (this) { payload = data; }
        nearbyExecutor.execute(() -> send(endpointId, payload));
    }

    private void send(@NonNull String endpointId, @NonNull Payload payload) {
        Nearby.getConnectionsClient(ctxt).sendPayload(endpointId, payload);
    }

    // This seems a little scary... no protocol version, no content identifier.  Whatevs...
    private Payload toPayload(@NonNull Set<URI> endpoints) {

        ObjectOutputStream oStream = null;
        try {
            final ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            oStream = new ObjectOutputStream(bStream);
            oStream.writeObject(new ArrayList<>(endpoints));
            oStream.flush();

            final Payload payload = Payload.fromBytes(bStream.toByteArray());
            synchronized (this) { data = payload; }
            return payload;
        }
        catch (IOException e) { Log.w(TAG, "Failed advertising servers", e); }
        finally {
            if (oStream != null) {
                try { oStream.close(); } catch (IOException ignore) { }
            }
        }

        throw new IllegalStateException("Failed creating payload");
    }
}
