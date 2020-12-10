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
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.couchbase.android.listsync.db.Db;
import com.couchbase.android.listsync.util.StringUtils;


@Singleton
public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginVM";

    public static final String STATUS_OK = "OK";

    private static final String ILLEGAL_USERNAME_CHARS = "\"*./:<>?\\|";


    @NonNull
    public final MutableLiveData<String> login = new MutableLiveData<>();

    @NonNull
    private final Db db;

    @Inject
    public LoginViewModel(@NonNull Db db) { this.db = db; }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    @NonNull
    public LiveData<String> login(@NonNull String user, @NonNull String pwd) {
        if (StringUtils.containsChar(user, ILLEGAL_USERNAME_CHARS)) {
            throw new IllegalArgumentException(String.format(
                "Username '%s' contains illegal an illegal character (%s)",
                user,
                ILLEGAL_USERNAME_CHARS));
        }

        login.setValue(null);

        db.openDb(user, pwd)
            .subscribe(
                () -> onLogin(user, STATUS_OK),
                e -> onLogin(user, e.toString()));

        return login;
    }

    public boolean isLoggedIn() { return !TextUtils.isEmpty(db.getUser()); }

    private void onLogin(@NonNull String user, @NonNull String message) {
        login.setValue(message);
        Log.d(TAG, "Login @" + user + ": " + message);
    }
}
