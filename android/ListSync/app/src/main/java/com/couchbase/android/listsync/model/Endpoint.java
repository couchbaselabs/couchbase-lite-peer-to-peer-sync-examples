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
import androidx.annotation.Nullable;

import java.net.URI;


public class Endpoint implements Comparable<Endpoint>, Named {
    @NonNull
    protected final URI uri;

    public Endpoint(@NonNull URI uri) { this.uri = uri; }

    @NonNull
    @Override
    public String getName() { return uri.toString(); }

    @NonNull
    public URI getUri() { return uri; }

    @SuppressWarnings("PMD.AvoidThrowingNullPointerException")
    @Override
    public int compareTo(@Nullable Endpoint o) {
        if (o == null) { throw new IllegalStateException("Endpoint is null in compare"); }
        return uri.compareTo(((Endpoint) o).uri);
    }

    @Override
    public int hashCode() { return uri.hashCode(); }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        return uri.equals(((Endpoint) o).uri);
    }
}

