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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.FragmentPermissionsBinding;
import com.couchbase.android.listsync.ui.p2p.P2PFragment;


public final class PermissionsFragment extends P2PFragment {
    public static final int PERMISSIONS_REQ = 57936;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private NearbyViewModel viewModel;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private FragmentPermissionsBinding binding;

    private Handler hdlr;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        viewModel = getViewModel(NearbyViewModel.class);

        binding = FragmentPermissionsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestNeededPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String ign1[], int[] ign2) {
        if (requestCode != PERMISSIONS_REQ) { return; }

        hdlr = null;
        binding.permsJustification.setVisibility(View.INVISIBLE);

        final List<String> missingPerms = checkPerms(viewModel.getRequiredPermissions());
        if (missingPerms.isEmpty()) { navigate(R.id.action_nav_perms_to_nearby); }

        boolean showRational = false;
        for (String perm: missingPerms) { showRational = showRational || shouldShowRequestPermissionRationale(perm); }
        if (!showRational) {
            Toast.makeText(getContext(), R.string.nearby_not_available, Toast.LENGTH_LONG).show();
            navigate(R.id.action_nav_perms_to_active);
            return;
        }

        binding.permsJustification.setVisibility(View.VISIBLE);
        hdlr = new Handler(Looper.getMainLooper());
        hdlr.postDelayed(this::requestNeededPermissions, 6 * 1000);
    }

    private void requestNeededPermissions() {
        final List<String> missingPerms = checkPerms(viewModel.getRequiredPermissions());
        requestPermissions(missingPerms.toArray(new String[0]), PERMISSIONS_REQ);
    }
}


