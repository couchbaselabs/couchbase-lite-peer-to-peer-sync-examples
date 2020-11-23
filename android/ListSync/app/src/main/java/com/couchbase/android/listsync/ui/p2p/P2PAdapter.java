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


public class P2PAdapter extends RecyclerView.Adapter<P2PAdapter.ConnectionViewHolder> {
    final class ConnectionViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RowSyncBinding bindings;

        @Nullable
        URI uri;

        ConnectionViewHolder(@NonNull RowSyncBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
            itemView.setOnClickListener(v -> setHighlighted(toggleSelection(this)));
        }

        public void setConnection(@Nullable URI uri) {
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

        public void setHighlighted(boolean highlighted) {
            itemView.setBackgroundColor(highlighted ? selectedBg : 0);
        }
    }

    @NonNull
    public static P2PAdapter setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView listView,
        @Nullable Runnable onSelectionChange) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        final P2PAdapter adapter = new P2PAdapter(
            onSelectionChange,
            ctxt.getResources().getColor(R.color.pale_yellow)
        );
        listView.setAdapter(adapter);

        return adapter;
    }


    @Nullable
    private final Runnable onSelectionChanged;
    private final int selectedBg;

    @Nullable
    private List<URI> connections;

    @Nullable
    private ConnectionViewHolder selected;

    public P2PAdapter(@Nullable Runnable onSelectionChanged, int selectedBg) {
        this.onSelectionChanged = onSelectionChanged;
        this.selectedBg = selectedBg;
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
        if (selected == vh) { clearSelection(); }
        super.onViewRecycled(vh);
    }

    public void populate(@Nullable Set<URI> connections) {
        if (connections == null) { this.connections = null; }
        else {
            final List<URI> sortedConnections = new ArrayList<>(connections);
            Collections.sort(sortedConnections);
            this.connections = sortedConnections;
        }
        notifyDataSetChanged();
        clearSelection();
    }

    @Nullable
    public URI getSelection() { return (selected == null) ? null : selected.uri; }

    public void clearSelection() {
        if (selected == null) { return; }
        selected.setHighlighted(false);
        select(null);
    }

    boolean toggleSelection(@NonNull ConnectionViewHolder newSelection) {
        if (selected == newSelection) { clearSelection(); }
        else {
            if (selected != null) { selected.setHighlighted(false); }
            select(newSelection);
        }
        return selected != null;
    }

    private void select(@Nullable ConnectionViewHolder selection) {
        selected = selection;
        if (onSelectionChanged != null) { onSelectionChanged.run(); }
    }
}
