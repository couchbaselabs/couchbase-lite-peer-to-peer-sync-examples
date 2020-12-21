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


public final class Device implements Named {
    @NonNull
    private final String id;
    @NonNull
    private final String name;

    public Device(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getId() { return id; }

    @NonNull
    public String getName() { return name; }

    @NonNull
    @Override
    public String toString() { return "Device{" + name + ", " + id + "}"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        return id.equals(((Device) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

