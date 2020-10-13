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
package com.couchbase.android.listsync.ui.main;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.model.Produce;


public class InSeasonAdapter extends RecyclerView.Adapter<InSeasonAdapter.InSeasonViewHolder> {
    static final class InSeasonViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RequestManager glide;
        @NonNull
        private final ImageView photoView;
        @NonNull
        private final TextView nameView;
        @NonNull
        private final TextView doneView;

        public InSeasonViewHolder(@NonNull View view, @NonNull RequestManager glide) {
            super(view);
            this.glide = glide;
            this.photoView = view.findViewById(R.id.photo);
            this.nameView = view.findViewById(R.id.name);
            this.doneView = view.findViewById(R.id.done);
        }

        public final void setProduce(@Nullable Produce produce) {
            nameView.setText((produce == null) ? "" : produce.getName());
            doneView.setText((produce == null) ? "" : String.valueOf(produce.getDone()));
            glide.load(produce).into(photoView);
        }
    }

    @NonNull
    public static InSeasonAdapter setup(@NonNull Activity ctxt, @NonNull RecyclerView listView) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        final InSeasonAdapter adapter = new InSeasonAdapter(ctxt);
        listView.setAdapter(adapter);

        return adapter;
    }


    @NonNull
    private final RequestManager glide;

    @Nullable
    private List<Produce> produce;

    public InSeasonAdapter(@NonNull Activity activity) { glide = Glide.with(activity); }

    public int getItemCount() { return (produce == null) ? 0 : produce.size(); }

    @NonNull
    public InSeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InSeasonViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.row_produce, parent, false),
            glide);
    }

    public void onBindViewHolder(@NonNull InSeasonViewHolder vh, int pos) {
        vh.setProduce(((produce == null) || (pos < 0) || (pos >= produce.size())) ? null : produce.get(pos));
    }

    public final void populate(@Nullable List<Produce> produce) {
        this.produce = produce;
        notifyDataSetChanged();
    }
}
