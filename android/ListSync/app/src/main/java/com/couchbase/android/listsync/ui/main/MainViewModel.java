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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.CompositeDisposable;

import com.couchbase.android.listsync.db.DatabaseManager;
import com.couchbase.android.listsync.model.Produce;
import com.couchbase.android.listsync.ui.login.LoginActivity;
import com.couchbase.android.listsync.ui.p2p.P2PActivity;


@Singleton
public class MainViewModel extends ViewModel {
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private final MutableLiveData<List<Produce>> inSeason = new MutableLiveData<>();

    @NonNull
    private final DatabaseManager db;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public MainViewModel(@NonNull DatabaseManager db) { this.db = db; }

    @NonNull
    public LiveData<List<Produce>> getInSeason() {
        disposables.add(db.getInSeason().subscribe(inSeason::setValue));
        return inSeason;
    }

    public void p2p(MainActivity activity) {
        db.closeDb();
        P2PActivity.start(activity);
    }

    public void logout(MainActivity activity) {
        db.closeDb();
        LoginActivity.start(activity);
        activity.finish();
    }

    public void cancel() { disposables.clear(); }
}
