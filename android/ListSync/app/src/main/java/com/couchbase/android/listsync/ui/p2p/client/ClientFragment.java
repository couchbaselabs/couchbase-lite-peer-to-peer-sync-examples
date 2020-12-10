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
package com.couchbase.android.listsync.ui.p2p.client;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.FragmentClientBinding;
import com.couchbase.android.listsync.db.Db;
import com.couchbase.android.listsync.model.Client;
import com.couchbase.android.listsync.ui.p2p.P2PFragment;
import com.couchbase.lite.URLEndpointListenerConfiguration;


public final class ClientFragment extends P2PFragment {
    private static final String DB_PATH = "/" + Db.DB_NAME;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ClientViewModel viewModel;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private FragmentClientBinding binding;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ClientAdapter adapter;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        viewModel = getViewModel(ClientViewModel.class);

        binding = FragmentClientBinding.inflate(inflater, container, false);
        final View root = binding.getRoot();

        binding.start.setOnClickListener(v -> startClient());

        final TextWatcher buttonEnabler = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { enableStartButton(); }
        };
        binding.host.addTextChangedListener(buttonEnabler);
        binding.port.addTextChangedListener(buttonEnabler);

        adapter = ClientAdapter.setup(
            getActivity(),
            binding.clients,
            this::restartClient,
            this::stopClient,
            this::deleteClient);

        initializeReplicatorUri();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.showMenu(-1);
        viewModel.getClients().observe(this, adapter::populate);
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.showMenu(-1);
        viewModel.cancel();
    }

    private void enableStartButton() {
        binding.start.setEnabled((binding.host.length() > 2) && (getPort() > 0));
    }

    private void startClient() {
        if (getPort() < 0) {
            Toast.makeText(getActivity(), R.string.bad_port, Toast.LENGTH_LONG).show();
            return;
        }

        final URI uri = getReplicatorUri();
        if (uri == null) { return; }

        clear();

        viewModel.startClient(uri);
    }

    private void restartClient(@Nullable Client client) {
        if (client == null) { return; }
        viewModel.restartClient(client);
    }

    private void stopClient(@Nullable Client client) {
        if (client == null) { return; }
        viewModel.stopClient(client);
    }

    private void deleteClient(@Nullable Client client) {
        if (client == null) { return; }
        viewModel.deleteClient(client);
    }

    private void clear() {
        binding.host.setText("");
        binding.port.setText("");
        enableStartButton();
    }

    @Nullable
    private URI getReplicatorUri() {
        try {
            final Editable host = binding.host.getText();
            if (host == null) { return null; }
            return new URI(ClientViewModel.SCHEME_WSS, null, host.toString(), getPort(), DB_PATH, null, null);
        }
        catch (URISyntaxException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void initializeReplicatorUri() {
        URI uri = null;
        final Bundle args = getArguments();
        if (args != null) { uri = ClientFragmentArgs.fromBundle(args).getUri(); }
        if (uri == null) {
            clear();
            return;
        }

        final String scheme = uri.getScheme();
        if (!ClientViewModel.SCHEME_WSS.equals(scheme)) {
            final String msg = getString(R.string.bad_scheme, scheme);
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        String suffix = "";
        String s = uri.getQuery();
        if (s != null) { suffix += "?" + s; }
        s = uri.getFragment();
        if (s != null) { suffix += "#" + s; }
        if (suffix.length() > 0) {
            final String msg = getString(R.string.bad_suffix, suffix);
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        final String path = uri.getPath();
        if (!DB_PATH.equals(path)) {
            final String msg = getString(R.string.bad_path, path);
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            return;
        }

        // the port and host should be ok?
        binding.host.setText(uri.getHost());
        binding.port.setText(String.valueOf(uri.getPort()));

        enableStartButton();
    }

    @NonNull
    private Integer getPort() {
        try {
            final Editable portStr = binding.port.getText();
            if (portStr == null) { return -1; }

            final int port = Integer.parseInt(portStr.toString());
            if ((port > URLEndpointListenerConfiguration.MIN_PORT)
                && (port <= URLEndpointListenerConfiguration.MAX_PORT)) {
                return port;
            }
        }
        catch (NumberFormatException ignore) { }
        return -1;
    }
}
