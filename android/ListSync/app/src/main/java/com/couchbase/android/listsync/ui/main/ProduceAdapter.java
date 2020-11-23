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
import android.view.ViewGroup;
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
import com.couchbase.android.listsync.databinding.RowProduceBinding;
import com.couchbase.android.listsync.model.Produce;


public class ProduceAdapter extends RecyclerView.Adapter<ProduceAdapter.ProduceViewHolder> {
    static final class ProduceViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final RowProduceBinding bindings;
        @NonNull
        private final MainViewModel viewModel;
        @NonNull
        private final RequestManager glide;

        ProduceViewHolder(
            @NonNull MainViewModel viewModel,
            @NonNull RowProduceBinding bindings,
            @NonNull RequestManager glide) {
            super(bindings.getRoot());

            this.bindings = bindings;
            this.viewModel = viewModel;
            this.glide = glide;
        }

        public void setProduce(@Nullable Produce produce) {
            if (produce == null) {
                bindings.name.setText("");
                bindings.done.setText("");
                return;
            }

            final String name = produce.getName();
            bindings.name.setText(name);

            bindings.done.setText(String.valueOf(produce.getDone()));
            bindings.done.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) { viewModel.updateDone(name, bindings.done.getText().toString()); }
            });

            glide.load(produce).into(bindings.photo);
        }
    }

    @NonNull
    public static ProduceAdapter setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView listView,
        @NonNull MainViewModel viewModel) {
        listView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        listView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(ctxt, R.drawable.divider));
        listView.addItemDecoration(divider);

        final ProduceAdapter adapter = new ProduceAdapter(ctxt, viewModel);
        listView.setAdapter(adapter);

        return adapter;
    }


    @NonNull
    private final MainViewModel viewModel;

    @NonNull
    private final RequestManager glide;

    @Nullable
    private List<Produce> produce;

    public ProduceAdapter(@NonNull Activity activity, @NonNull MainViewModel viewModel) {
        this.viewModel = viewModel;
        glide = Glide.with(activity);
    }

    public int getItemCount() { return (produce == null) ? 0 : produce.size(); }

    @NonNull
    public ProduceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProduceViewHolder(
            viewModel,
            RowProduceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
            glide);
    }

    public void onBindViewHolder(@NonNull ProduceViewHolder vh, int pos) {
        vh.setProduce(((produce == null) || (pos < 0) || (pos >= produce.size())) ? null : produce.get(pos));
    }

    public final void populate(@Nullable List<Produce> produce) {
        this.produce = produce;
        notifyDataSetChanged();
    }
}
