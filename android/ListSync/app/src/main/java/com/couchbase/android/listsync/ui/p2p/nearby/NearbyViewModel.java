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
package com.couchbase.android.listsync.ui.p2p.nearby;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.disposables.Disposable;

import com.couchbase.android.listsync.model.Device;
import com.couchbase.android.listsync.model.Listener;
import com.couchbase.android.listsync.net.nearby.NearbyClient;


@Singleton
public class NearbyViewModel extends ViewModel {
    private static final String TAG = "NEAR_VM";

    private static final List<String> REQUIRED_PERMISSIONS;
    static {
        final List<String> l = new ArrayList<>();
        l.add(android.Manifest.permission.BLUETOOTH);
        l.add(android.Manifest.permission.BLUETOOTH_ADMIN);
        l.add(android.Manifest.permission.ACCESS_WIFI_STATE);
        l.add(android.Manifest.permission.CHANGE_WIFI_STATE);
        l.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS = Collections.unmodifiableList(l);
    }


    @NonNull
    private final MutableLiveData<Collection<Device>> nearbyDevices = new MutableLiveData<>();

    @NonNull
    private final MutableLiveData<Collection<Listener>> nearbyListeners = new MutableLiveData<>();

    @NonNull
    private final NearbyClient nearby;

    @Nullable
    private Disposable deviceDisposable;
    @Nullable
    private Disposable listenerDisposable;

    @Inject
    public NearbyViewModel(@NonNull NearbyClient nearby) { this.nearby = nearby; }

    @NonNull
    public LiveData<Collection<Device>> getNearbyDevices() {
        if (deviceDisposable == null) {
            deviceDisposable = nearby.startDiscovery().subscribe(
                nearbyDevices::setValue,
                e -> Log.w(TAG, "Discovery failed", e));
        }
        return nearbyDevices;
    }

    @NonNull
    public LiveData<Collection<Listener>> getNearbyListeners(@Nullable Device device) {
        cancelListeners();

        if (device == null) {
            nearbyListeners.setValue(null);
        }
        else {
            listenerDisposable = nearby.getListenersForDevice(device).subscribe(
                nearbyListeners::setValue,
                e -> Log.w(TAG, "Failed getting listeners for device " + device, e));
        }

        return nearbyListeners;
    }

    public void cancel() {
        cancelListeners();
        if (deviceDisposable != null) {
            deviceDisposable.dispose();
            deviceDisposable = null;
        }
    }

    private void cancelListeners() {
        if (listenerDisposable == null) { return; }
        listenerDisposable.dispose();
        listenerDisposable = null;
    }

    public String[] getRequiredPermissions() { return REQUIRED_PERMISSIONS.toArray(new String[0]); }
}
