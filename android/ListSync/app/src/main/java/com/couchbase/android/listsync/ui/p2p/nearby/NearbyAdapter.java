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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.RowNearbyBinding;
import com.couchbase.android.listsync.model.Endpoint;
import com.couchbase.lite.internal.utils.Fn;


public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.NearbyViewHolder> {
    final class NearbyViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RowNearbyBinding bindings;

        @Nullable
        Endpoint endpoint;

        public NearbyViewHolder(@NonNull RowNearbyBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
            itemView.setOnClickListener(v -> setHighlighted(toggleSelection(this)));
        }

        public final void setNearby(@Nullable Endpoint endpoint) {
            this.endpoint = endpoint;
            bindings.nearby.setText((endpoint == null) ? "" : endpoint.getName());
        }

        public void setHighlighted(boolean highlighted) {
            itemView.setBackgroundColor(highlighted ? selectedBg : 0);
        }
    }

    @NonNull
    public static NearbyAdapter setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView listView,
        @Nullable Fn.Consumer<Endpoint> onSelectionChange) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        final NearbyAdapter adapter = new NearbyAdapter(
            onSelectionChange,
            ctxt.getResources().getColor(R.color.pale_yellow)
        );
        listView.setAdapter(adapter);

        return adapter;
    }


    @Nullable
    private final Fn.Consumer<Endpoint> onSelectionChanged;
    private final int selectedBg;

    @Nullable
    private List<Endpoint> nearby;

    @Nullable
    private NearbyViewHolder selected;

    public NearbyAdapter(@Nullable Fn.Consumer<Endpoint> onSelectionChanged, int selectedBg) {
        this.onSelectionChanged = onSelectionChanged;
        this.selectedBg = selectedBg;
    }

    @Override
    public int getItemCount() { return (nearby == null) ? 0 : nearby.size(); }

    @NonNull
    @Override
    public NearbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NearbyViewHolder(
            RowNearbyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyViewHolder vh, int pos) {
        vh.setNearby(((nearby == null) || (pos < 0) || (pos >= nearby.size()))
            ? null
            : nearby.get(pos));
    }

    public void populate(@Nullable Collection<Endpoint> nearbys) {
        final List<Endpoint> sortedNearbys = new ArrayList<>(nearbys);
        Collections.sort(sortedNearbys, (e1, e2) -> e1.getName().compareTo(e2.getName()));
        this.nearby = sortedNearbys;
        notifyDataSetChanged();
        clearSelection();
    }

    @Nullable
    public Endpoint getSelection() { return (selected == null) ? null : selected.endpoint; }

    public void clearSelection() {
        if (selected == null) { return; }
        selected.setHighlighted(false);
        select(null);
    }

    boolean toggleSelection(@NonNull NearbyViewHolder newSelection) {
        if (selected == newSelection) { clearSelection(); }
        else {
            if (selected != null) { selected.setHighlighted(false); }
            select(newSelection);
        }
        return selected != null;
    }

    private void select(@Nullable NearbyViewHolder selection) {
        selected = selection;
        if (onSelectionChanged != null) { onSelectionChanged.accept((selection == null) ? null : selection.endpoint); }
    }
}