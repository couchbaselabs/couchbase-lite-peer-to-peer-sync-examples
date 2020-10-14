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
package com.couchbase.android.listsync.ui.login;

import android.annotation.SuppressLint;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.couchbase.android.listsync.db.DatabaseManager;


@Singleton
public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginVM";

    public static final String STATUS_OK = "OK";

    @NonNull
    public final MutableLiveData<String> login = new MutableLiveData<>();

    @NonNull
    private final DatabaseManager db;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LoginViewModel(@NonNull DatabaseManager db) { this.db = db; }

    @NonNull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public LiveData<String> login(@NonNull String user, @NonNull String pwd) {
        db.openDb(user, pwd)
            .subscribe(
                () -> login.setValue(STATUS_OK),
                e -> {
                    Log.w(TAG, "Failed opening db", e);
                    login.setValue(e.toString());
                });
        return login;
    }

    public boolean isLoggedIn() { return db.isLoggedIn(); }
}
