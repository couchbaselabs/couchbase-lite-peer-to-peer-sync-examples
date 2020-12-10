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
import com.couchbase.android.listsync.model.Named;
import com.couchbase.lite.internal.utils.Fn;


public class NearbyAdapter<T extends Named> extends RecyclerView.Adapter<NearbyAdapter<T>.NearbyViewHolder> {
    final class NearbyViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RowNearbyBinding bindings;

        @Nullable
        T data;

        NearbyViewHolder(@NonNull RowNearbyBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
            itemView.setOnClickListener(v -> setHighlighted(toggleSelection(this)));
        }

        public void setNearby(@Nullable T data) {
            this.data = data;
            bindings.nearby.setText((data == null) ? "" : data.getName());
        }

        public void setHighlighted(boolean highlighted) {
            itemView.setBackgroundColor(highlighted ? selectedBg : 0);
        }
    }

    @NonNull
    public static <S extends Named> NearbyAdapter<S> setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView listView,
        @Nullable Fn.Consumer<S> onSelectionChange) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        final NearbyAdapter<S> adapter = new NearbyAdapter<>(
            onSelectionChange,
            ctxt.getResources().getColor(R.color.khaki)
        );
        listView.setAdapter(adapter);

        return adapter;
    }


    @Nullable
    private final Fn.Consumer<T> onSelectionChanged;
    private final int selectedBg;

    @Nullable
    private List<T> nearby;

    @Nullable
    private NearbyViewHolder selected;

    public NearbyAdapter(@Nullable Fn.Consumer<T> onSelectionChanged, int selectedBg) {
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

    public void populate(@Nullable Collection<T> nearbys) {
        final List<T> sortedNearbys = (nearbys != null) ? new ArrayList<>(nearbys) : Collections.emptyList();
        Collections.sort(sortedNearbys, (e1, e2) -> e1.getName().compareTo(e2.getName()));
        this.nearby = sortedNearbys;
        notifyDataSetChanged();
        clearSelection();
    }

    @Nullable
    public T getSelection() { return (selected == null) ? null : selected.data; }

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
        if (onSelectionChanged != null) { onSelectionChanged.accept((selection == null) ? null : selection.data); }
    }
}
