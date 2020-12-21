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

import java.util.Objects;

import com.couchbase.lite.Blob;


public final class Produce implements Named {
    @NonNull
    private final String name;
    @Nullable
    private final Blob photo;
    private final long done;

    public Produce(@NonNull String name, @Nullable Blob photo, long done) {
        this.name = name;
        this.photo = photo;
        this.done = done;
    }

    @NonNull
    public String getName() { return name; }

    @Nullable
    public Blob getPhoto() { return photo; }

    public long getDone() { return done; }

    @NonNull
    @Override
    public String toString() {
        String s = "Produce{" + name + ", " + done + ",";
        if (photo != null) { s += photo.toString(); }
        return s + "}";
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null) { return false; }
        if (!(o instanceof Produce)) { return false; }
        final Produce produce = (Produce) o;
        return name.equals(produce.name);
    }
}

