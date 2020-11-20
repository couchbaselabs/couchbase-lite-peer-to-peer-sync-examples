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
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.android.gms.nearby.connection.Payload;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import com.couchbase.android.listsync.db.Db;


abstract class BaseNearby {
    private static final String TAG = "NEARBY_BASE";

    // This seems a little scary... no protocol version, no content identifier.  Whatevs...
    @Nullable
    protected static Payload toPayload(@Nullable List<URI> endpoints) {
        if (endpoints == null) { return null; }

        try (ByteArrayOutputStream bStream = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oStream = new ObjectOutputStream(bStream)) {
                oStream.writeObject(endpoints);
                oStream.flush();
                return Payload.fromBytes(bStream.toByteArray());
            }
        }
        catch (IOException e) {
            Log.w(TAG, "Failed encoding payload", e);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static List<URI> fromPayload(@Nullable byte[] payloadData) {
        if (payloadData == null) { return null; }

        try (ByteArrayInputStream bStream = new ByteArrayInputStream(payloadData)) {
            try (ObjectInputStream oStream = new ObjectInputStream(bStream)) {
                return (List<URI>) oStream.readObject();
            }
        }
        catch (IOException | ClassNotFoundException e) {
            Log.w(TAG, "Failed decoding payload", e);
        }

        return null;
    }

    @NonNull
    protected final Executor nearbyExecutor = Executors.newSingleThreadExecutor();
    @NonNull
    protected final Scheduler nearbyScheduler = Schedulers.from(nearbyExecutor);

    @NonNull
    protected final Context ctxt;
    @NonNull
    protected final String user;
    @NonNull
    protected final String pkgName;

    protected BaseNearby(@NonNull Context ctxt, @NonNull Db db) {
        this.ctxt = ctxt.getApplicationContext();
        this.pkgName = ctxt.getPackageName();
        this.user = db.getUser();
        if (TextUtils.isEmpty(user)) { throw new IllegalStateException("Attempt to use nearby before sign in"); }
    }
}
