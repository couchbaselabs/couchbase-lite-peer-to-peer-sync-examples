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
package com.couchbase.android.listsync.app;

import android.app.Activity;
import android.app.Application;
import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.couchbase.android.listsync.ui.p2p.client.ClientAdapter;


public class ListSync extends Application implements HasActivityInjector {
    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @NonNull
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    public void onCreate() {
        super.onCreate();

        DaggerAppFactory.builder().create(this).inject(this);

        ClientAdapter.init(this);
    }

    @NonNull
    @Override
    public AndroidInjector<Activity> activityInjector() { return dispatchingActivityInjector; }
}
