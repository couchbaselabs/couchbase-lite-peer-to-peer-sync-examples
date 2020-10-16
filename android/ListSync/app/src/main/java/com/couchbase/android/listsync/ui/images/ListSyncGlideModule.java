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


import android.content.Context;
import androidx.annotation.NonNull;

import java.io.InputStream;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.AppGlideModule;

import com.couchbase.android.listsync.model.Produce;


@GlideModule
public class ListSyncGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context ctxt, @NonNull GlideBuilder builder) { }

    @Override
    public void registerComponents(@NonNull Context ctxt, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(
            Produce.class,
            InputStream.class,
            new ModelLoaderFactory<Produce, InputStream>() {
                @NonNull
                @Override
                public ModelLoader<Produce, InputStream> build(@NonNull MultiModelLoaderFactory ignore) {
                    return new ProduceImageLoader();
                }

                @Override
                public void teardown() { }
            });
    }
}
