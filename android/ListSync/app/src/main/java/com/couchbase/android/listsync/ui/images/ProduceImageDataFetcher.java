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

import android.util.Log;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import com.couchbase.android.listsync.model.Produce;
import com.couchbase.lite.Blob;


class ProduceImageDataFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "IMG";

    private final Produce produce;

    private InputStream blobStream;

    ProduceImageDataFetcher(@NonNull Produce produce) { this.produce = produce; }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        final Blob blob = produce.getPhoto();
        blobStream = (blob == null) ? null : blob.getContentStream();
        callback.onDataReady(blobStream);
    }

    @Override
    public void cleanup() {
        if (blobStream == null) { return; }

        try { blobStream.close(); }
        catch (IOException e) { Log.w(TAG, "Failed closing blob input stream for " + produce.getName()); }
    }


    @Override
    public void cancel() {}

    @NonNull
    @Override
    public Class<InputStream> getDataClass() { return InputStream.class; }

    @NonNull
    @Override
    public DataSource getDataSource() { return DataSource.LOCAL; }
}
