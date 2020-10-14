//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
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
package com.couchbase.android.listsync.ui;

import android.content.Context;
import androidx.annotation.NonNull;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

import com.couchbase.android.listsync.ui.login.LoginActivity;
import com.couchbase.android.listsync.ui.main.MainActivity;
import com.couchbase.android.listsync.app.ListSync;
import com.couchbase.android.listsync.ui.p2p.P2PActivity;


@Module
public interface ActivityBuilder {
    @NonNull
    @Binds
    Context appContext(ListSync app);

    @NonNull
    @ContributesAndroidInjector
    LoginActivity bindLoginActivity();

    @NonNull
    @ContributesAndroidInjector
    MainActivity bindMainActivity();

    @NonNull
    @ContributesAndroidInjector
    P2PActivity bindP2PActivity();
}
