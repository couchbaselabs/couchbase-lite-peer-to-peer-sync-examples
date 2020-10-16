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
package com.couchbase.android.listsync.ui.p2p;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.RowSyncBinding;


public class SyncAdapter extends RecyclerView.Adapter<SyncAdapter.ConnectionViewHolder> {
    final class ConnectionViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RowSyncBinding bindings;

        @Nullable
        URI uri;

        public ConnectionViewHolder(@NonNull RowSyncBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
            itemView.setOnClickListener(this::select);
        }

        public final void setConnection(@Nullable URI uri) {
            this.uri = uri;

            if (uri == null) {
                bindings.host.setText("");
                bindings.port.setText("");
                bindings.database.setText("");
                bindings.url.setText("");
                return;
            }

            String dbName = uri.getPath();
            final int i = dbName.indexOf('/');
            if (i >= 0) { dbName = dbName.substring(i + 1); }

            bindings.host.setText(uri.getHost());
            bindings.port.setText(String.valueOf(uri.getPort()));
            bindings.database.setText(dbName);
            bindings.url.setText(uri.toString());
        }

        private void select(View view) {
            itemView.setBackgroundColor(toggleSelection(this) ? selectedBg : unselectedBg);
        }
    }

    @NonNull
    public static SyncAdapter setup(@NonNull Activity ctxt, @NonNull RecyclerView listView) {
        return setup(ctxt, listView, null);
    }

    @NonNull
    public static SyncAdapter setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView listView,
        @Nullable Runnable onSelectionChange) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        ctxt.getResources().getColor(R.color.pale_yellow);

        final SyncAdapter adapter = new SyncAdapter(
            onSelectionChange,
            ctxt.getResources().getColor(R.color.white),
            ctxt.getResources().getColor(R.color.pale_yellow));
        listView.setAdapter(adapter);

        return adapter;
    }


    @NonNull
    private final Runnable onSelectionChanged;
    private final int selectedBg;
    private final int unselectedBg;

    @Nullable
    private List<URI> connections;

    @Nullable
    private ConnectionViewHolder selected;

    public SyncAdapter(@Nullable Runnable onSelectionChanged, int selectedBg, int unselectedBg) {
        this.onSelectionChanged = onSelectionChanged;
        this.selectedBg = selectedBg;
        this.unselectedBg = unselectedBg;
    }

    @Override
    public int getItemCount() { return (connections == null) ? 0 : connections.size(); }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConnectionViewHolder(
            RowSyncBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder vh, int pos) {
        vh.setConnection(((connections == null) || (pos < 0) || (pos >= connections.size()))
            ? null
            : connections.get(pos));
    }

    @Override
    public void onViewRecycled(@NonNull ConnectionViewHolder vh) {
        deselect(vh);
        super.onViewRecycled(vh);
    }

    public void populate(@Nullable Set<URI> connections) {
        final List<URI> sortedConnections = new ArrayList<>(connections);
        Collections.sort(sortedConnections);
        this.connections = sortedConnections;
        notifyDataSetChanged();
    }

    @Nullable
    public URI getFirstConnection() { return (connections.size() <= 0) ? null : connections.get(0); }

    public boolean isSelected(@Nullable ConnectionViewHolder vh) { return selected == vh; }

    @Nullable
    public URI getAndClearSelection() {
        if (selected == null) { return null; }
        final ConnectionViewHolder curSelection = selected;
        select(null);
        return curSelection.uri;
    }

    boolean toggleSelection(@NonNull ConnectionViewHolder selection) {
        select(isSelected(selection) ? null : selection);
        return isSelected(selection);
    }

    void deselect(@NonNull ConnectionViewHolder vh) {
        if (isSelected(vh)) { select(null); }
    }

    private void select(@Nullable ConnectionViewHolder selection) {
        selected = selection;
        if (onSelectionChanged != null) { onSelectionChanged.run(); }
    }
}