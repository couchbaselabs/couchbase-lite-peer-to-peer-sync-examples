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

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

import com.couchbase.android.listsync.ui.client.ClientViewModel;
import com.couchbase.android.listsync.ui.login.LoginViewModel;
import com.couchbase.android.listsync.ui.main.MainViewModel;
import com.couchbase.android.listsync.ui.server.ServerViewModel;


@SuppressWarnings({"WeakerAccess"})
@Module
public interface VMModule {
    @NonNull
    @Binds
    ViewModelProvider.Factory bindsViewModelFactory(@NonNull ViewModelFactory viewModelFactory);

    @NonNull
    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    ViewModel bindLoginViewModel(@NonNull LoginViewModel loginViewModel);

    @NonNull
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    ViewModel bindMainViewModel(@NonNull MainViewModel mainViewModel);

    @NonNull
    @Binds
    @IntoMap
    @ViewModelKey(ServerViewModel.class)
    ViewModel bindServerViewModel(@NonNull ServerViewModel serverViewModel);

    @NonNull
    @Binds
    @IntoMap
    @ViewModelKey(ClientViewModel.class)
    ViewModel bindClientViewModel(@NonNull ClientViewModel clientViewModel);
}
