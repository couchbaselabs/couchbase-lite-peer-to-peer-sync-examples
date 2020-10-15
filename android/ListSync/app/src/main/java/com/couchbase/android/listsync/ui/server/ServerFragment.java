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
package com.couchbase.android.listsync.ui.server;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.couchbase.android.listsync.databinding.FragmentServerBinding;
import com.couchbase.android.listsync.ui.p2p.BaseFragment;


public final class ServerFragment extends BaseFragment {

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ServerViewModel viewModel;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private FragmentServerBinding binding;

    @NonNull
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        viewModel = getViewModel(ServerViewModel.class);

        binding = FragmentServerBinding.inflate(inflater, container, false);
        final View root = binding.getRoot();

        binding.start.setOnClickListener(v -> startListener());
        binding.stop.setOnClickListener(v -> stopListener());

        return root;
    }

    private void startListener() {
        final LiveData<String> endpointData = viewModel.startListener();
        endpointData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String endpoint) {
                binding.start.setEnabled(false);
                binding.stop.setEnabled(true);
                binding.endpoint.setText(endpoint);
                endpointData.removeObserver(this);
            }
        });
    }

    private void stopListener() {
        viewModel.stopListener();
        binding.start.setEnabled(true);
        binding.stop.setEnabled(false);
        binding.endpoint.setText("");
    }
}
