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
package com.couchbase.android.listsync.ui.images;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import com.couchbase.android.listsync.model.Produce;
import com.couchbase.lite.Blob;


public final class ProduceImageLoader implements ModelLoader<Produce, InputStream> {
    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull Produce produce, int w, int h, @NonNull Options opts) {
        final Blob photo = produce.getPhoto();
        if (photo == null) { return null; }
        final String digest = photo.digest();
        if (digest == null) { return null; }
        return new LoadData<>(new ObjectKey(digest), new ProduceImageDataFetcher(produce));
    }

    @Override
    public boolean handles(@NonNull Produce produce) { return produce.getPhoto() != null; }
}