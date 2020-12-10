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
package com.couchbase.android.listsync.model;

import androidx.annotation.NonNull;

import java.net.URI;
import java.util.Objects;

import com.couchbase.lite.AbstractReplicator;


public class Client extends Endpoint {
    @NonNull
    private final AbstractReplicator.ActivityLevel activityLevel;

    public Client(@NonNull URI uri, @NonNull AbstractReplicator.ActivityLevel activityLevel) {
        super(uri);
        this.activityLevel = activityLevel;
    }

    @NonNull
    public AbstractReplicator.ActivityLevel getActivityLevel() { return activityLevel; }

    @NonNull
    @Override
    public String toString() { return "Client{" + activityLevel + " @" + uri + "}"; }

    @Override
    public int hashCode() { return Objects.hash(uri); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null) { return false; }
        if (!(o instanceof Client)) { return false; }
        final Client client = (Client) o;
        return uri.equals(client.uri);
    }
}
