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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.FragmentPermissionsBinding;
import com.couchbase.android.listsync.ui.p2p.P2PFragment;


public final class PermissionsFragment extends P2PFragment {
    public static final int PERMISSIONS_REQ = 57936;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private NearbyViewModel viewModel;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private FragmentPermissionsBinding binding;

    @SuppressWarnings("PMD.SingularField")
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] ign1, @NonNull int[] ign2) {
        if (requestCode != PERMISSIONS_REQ) { return; }

        hdlr = null;
        binding.permsJustification.setVisibility(View.INVISIBLE);
        final NavController nav = Navigation.findNavController(binding.getRoot());

        final List<String> missingPerms = checkPerms(viewModel.getRequiredPermissions());
        if (missingPerms.isEmpty()) {
            nav.navigate(PermissionsFragmentDirections.actionNavPermsToNearby());
            return;
        }

        boolean showRational = false;
        for (String perm: missingPerms) { showRational = showRational || shouldShowRequestPermissionRationale(perm); }
        if (!showRational) {
            Toast.makeText(getContext(), R.string.nearby_not_available, Toast.LENGTH_LONG).show();
            nav.navigate(PermissionsFragmentDirections.actionNavPermsToActive());
            return;
        }

        // show the justification and then re-request permissions.
        binding.permsJustification.setVisibility(View.VISIBLE);
        hdlr = new Handler(Looper.getMainLooper());
        hdlr.postDelayed(this::requestNeededPermissions, 6 * 1000);
    }

    private void requestNeededPermissions() {
        final List<String> missingPerms = checkPerms(viewModel.getRequiredPermissions());
        requestPermissions(missingPerms.toArray(new String[0]), PERMISSIONS_REQ);
    }
}


