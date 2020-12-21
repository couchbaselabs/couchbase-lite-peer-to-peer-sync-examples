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
package com.couchbase.android.listsync.ui.main;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

import com.couchbase.android.listsync.db.Db;
import com.couchbase.android.listsync.model.Produce;
import com.couchbase.android.listsync.ui.login.LoginActivity;
import com.couchbase.android.listsync.ui.p2p.P2PActivity;


@Singleton
public class MainViewModel extends ViewModel {
    private static final String TAG = "MAIN_VM";

    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final MutableLiveData<List<Produce>> produce = new MutableLiveData<>();

    @NonNull
    private final Db db;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public MainViewModel(@NonNull Db db) { this.db = db; }

    public boolean loggedIn() { return !TextUtils.isEmpty(db.getUser()); }

    @NonNull
    public LiveData<List<Produce>> getProduce() {
        disposables.add(db.getProduce().subscribe(produce::setValue));
        return produce;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    public void updateDone(@NonNull String name, @NonNull String done) {
        try {
            db.updateDone(name, Long.parseLong(done))
                .subscribe(
                    () -> {},
                    (e) -> Log.w(TAG, "DB save failed for: " + name + " => " + done));
        }
        catch (NumberFormatException e) {
            // this should never happen: input type on the edit text should prevent it.
            Log.w(TAG, "'done' string is not a number: " + done);
        }
    }

    public void p2p(MainActivity activity) { P2PActivity.start(activity); }

    public void logout(MainActivity activity) {
        closeDb();
        LoginActivity.start(activity);
        activity.finish();
    }

    public void cancel() { disposables.clear(); }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void closeDb() {
        db.closeDb().subscribe(
            () -> Log.i(TAG, "Db closed"),
            (e) -> Log.w(TAG, "Failed to close db", e));
    }
}
