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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.FragmentNearbyBinding;
import com.couchbase.android.listsync.model.Device;
import com.couchbase.android.listsync.model.Listener;
import com.couchbase.android.listsync.ui.p2p.P2PFragment;


public final class NearbyFragment extends P2PFragment {
    public static final int PERMISSIONS_REQ = 57936;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private NearbyViewModel viewModel;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private FragmentNearbyBinding binding;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private NearbyAdapter<Device> nearbyDeviceAdapter;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private NearbyAdapter<Listener> nearbyListenerAdapter;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        viewModel = getViewModel(NearbyViewModel.class);

        binding = FragmentNearbyBinding.inflate(inflater, container, false);
        final View root = binding.getRoot();

        nearbyDeviceAdapter = NearbyAdapter.setup(getActivity(), binding.nearbyDevices, this::selectNearbyDevice);

        nearbyListenerAdapter = NearbyAdapter.setup(getActivity(), binding.nearbyListeners, this::selectNearbyListener);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (checkPerms(viewModel.getRequiredPermissions()).isEmpty()) { return; }
        navigate(R.id.action_nav_nearby_to_perms);
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.getNearbyDevices().observe(this, nearbyDeviceAdapter::populate);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.cancel();
    }

    private void selectNearbyDevice(@Nullable Device device) {
        viewModel.getNearbyListeners(device).observe(this, nearbyListenerAdapter::populate);
    }

    private void selectNearbyListener(@Nullable Listener listener) {
        final NearbyFragmentDirections.ActionNavNearbyToActive direction
            = NearbyFragmentDirections.actionNavNearbyToActive();
        if (listener != null) { direction.setUri(listener.getUri()); }
        navigate(direction);
    }
}
