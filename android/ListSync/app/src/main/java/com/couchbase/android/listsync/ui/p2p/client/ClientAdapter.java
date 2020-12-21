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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.RowClientBinding;
import com.couchbase.android.listsync.databinding.RowMenuBinding;
import com.couchbase.android.listsync.model.Client;
import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.internal.utils.Fn;


public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolderBase> {
    private static final String TAG = "CLIENT_AD";

    private static final int MENU_VIEW = 66;
    private static final int CLIENT_VIEW = 67;

    static final class ReplicatorState {
        final String name;
        final int color;

        ReplicatorState(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    static abstract class ViewHolderBase extends RecyclerView.ViewHolder {
        ViewHolderBase(@NonNull View rootView) { super(rootView); }

        public abstract void setClient(@Nullable Client client);
    }

    final class MenuViewHolder extends ViewHolderBase {
        @NonNull
        private final RowMenuBinding bindings;

        MenuViewHolder(@NonNull RowMenuBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
        }

        public void setClient(@Nullable Client client) {
            this.bindings.start.setOnClickListener(v -> onAction(() -> onRestart.accept(client)));
            this.bindings.stop.setOnClickListener(v -> onAction(() -> onStop.accept(client)));
            this.bindings.delete.setOnClickListener(v -> onAction(() -> onDelete.accept(client)));

            if (client == null) {
                bindings.host.setText("");
                bindings.port.setText("");
                return;
            }

            final URI uri = client.getUri();
            bindings.host.setText(uri.getHost());
            bindings.port.setText(String.valueOf(uri.getPort()));
        }

        private void onAction(@NonNull Runnable action) {
            showMenu(-1);
            action.run();
        }
    }

    static final class ClientViewHolder extends ViewHolderBase {
        @NonNull
        private final RowClientBinding bindings;

        ClientViewHolder(@NonNull RowClientBinding bindings) {
            super(bindings.getRoot());
            this.bindings = bindings;
        }

        public void setClient(@Nullable Client client) {
            if (client == null) {
                bindings.host.setText("");
                bindings.port.setText("");
                bindings.url.setText("");
                return;
            }

            final URI uri = client.getUri();
            bindings.host.setText(uri.getHost());
            bindings.port.setText(String.valueOf(uri.getPort()));
            bindings.url.setText(uri.toString());

            final ReplicatorState state = replicatorStates.get(client.getActivityLevel());
            if (state != null) {
                bindings.state.setTextColor(state.color);
                bindings.state.setText(state.name);
            }
        }
    }

    class ClientTouchCallback extends ItemTouchHelper.SimpleCallback {
        public static final float DRAG_RATE = 0.33F;

        private final View leftFill;
        private final ColorDrawable rightFill;

        @Nullable
        private RecyclerView.ViewHolder cachedVH;

        ClientTouchCallback(@NonNull View leftFill, @NonNull ColorDrawable rightFill) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.leftFill = leftFill;
            this.rightFill = rightFill;
        }

        @Override
        public boolean onMove(
            @NotNull RecyclerView v,
            @NotNull RecyclerView.ViewHolder vh,
            @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        /*
         * Swipe left iff client view; right iff menu view
         */
        @Override
        public void onSwiped(@NotNull RecyclerView.ViewHolder vh, int dir) {
            if ((vh instanceof MenuViewHolder) && (dir == ItemTouchHelper.RIGHT)) { showMenu(-1); }

            if ((vh instanceof ClientViewHolder) && (dir == ItemTouchHelper.LEFT)) {
                showMenu(vh.getAdapterPosition());
            }
        }

        @Override
        public void onChildDraw(
            @NotNull Canvas c,
            @NotNull RecyclerView v,
            @NotNull RecyclerView.ViewHolder vh,
            float dX,
            float dY,
            int state,
            boolean active) {
            dX = dX * DRAG_RATE;

            // Swipe left iff client view; right iff menu view
            if ((Math.abs(0.0F - dX) < 0.001F)
                || ((vh instanceof ClientViewHolder) && (dX > 0))
                || ((vh instanceof MenuViewHolder) && (dX < 0))) {
                return;
            }

            super.onChildDraw(c, v, vh, dX, 0, state, active);

            final View itemView = vh.itemView;
            final int l = itemView.getLeft();
            final int t = itemView.getTop();
            final int r = itemView.getRight();
            final int b = itemView.getBottom();
            final int idX = Math.round(dX);

            // fill right...
            if (dX >= 0) {
                rightFill.setBounds(l, t, l + idX, b);
                rightFill.draw(c);
                return;
            }

            // fill left...
            final int h = b - t;

            if (!Objects.equals(vh, cachedVH)) {
                leftFill.measure(
                    View.MeasureSpec.makeMeasureSpec(r - l, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
                leftFill.layout(l, t, r, b);
                cachedVH = vh;
            }

            c.save();
            c.translate(l, t);
            c.clipRect(r + idX, 0, r, h);

            leftFill.draw(c);

            c.restore();
        }
    }

    private static Map<AbstractReplicator.ActivityLevel, ReplicatorState> replicatorStates;
    private static Drawable divider;

    public static void init(@NonNull Context ctxt) {
        final Resources rez = ctxt.getResources();

        divider = ContextCompat.getDrawable(ctxt, R.drawable.divider);

        final Map<AbstractReplicator.ActivityLevel, ReplicatorState> states = new HashMap<>();
        states.put(
            AbstractReplicator.ActivityLevel.CONNECTING,
            new ReplicatorState(rez.getString(R.string.state_connecting), rez.getColor(R.color.state_connecting)));
        states.put(
            AbstractReplicator.ActivityLevel.BUSY,
            new ReplicatorState(rez.getString(R.string.state_busy), rez.getColor(R.color.state_busy)));
        states.put(
            AbstractReplicator.ActivityLevel.IDLE,
            new ReplicatorState(rez.getString(R.string.state_idle), rez.getColor(R.color.state_idle)));
        states.put(
            AbstractReplicator.ActivityLevel.OFFLINE,
            new ReplicatorState(rez.getString(R.string.state_offline), rez.getColor(R.color.state_offline)));
        states.put(
            AbstractReplicator.ActivityLevel.STOPPED,
            new ReplicatorState(rez.getString(R.string.state_stopped), rez.getColor(R.color.state_stopped)));
        replicatorStates = Collections.unmodifiableMap(states);
    }

    @NonNull
    public static ClientAdapter setup(
        @NonNull Activity ctxt,
        @NonNull RecyclerView clientView,
        @Nullable Fn.Consumer<Client> onRestart,
        @Nullable Fn.Consumer<Client> onStop,
        @Nullable Fn.Consumer<Client> onDelete) {
        clientView.hasFixedSize();

        final LinearLayoutManager layoutMgr = new LinearLayoutManager(ctxt);
        clientView.setLayoutManager(layoutMgr);

        final DividerItemDecoration divider = new DividerItemDecoration(ctxt, layoutMgr.getOrientation());
        divider.setDrawable(ClientAdapter.divider);
        clientView.addItemDecoration(divider);

        final ClientAdapter adapter = new ClientAdapter(onRestart, onStop, onDelete);
        clientView.setAdapter(adapter);

        adapter.setTouchHelper(clientView);

        clientView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView view, int state) {
                adapter.showMenu(-1);
                super.onScrollStateChanged(view, state);
            }
        });

        return adapter;
    }


    @Nullable
    private final Fn.Consumer<Client> onRestart;
    @Nullable
    private final Fn.Consumer<Client> onStop;
    @Nullable
    private final Fn.Consumer<Client> onDelete;

    @NonNull
    private List<Client> clients = Collections.emptyList();

    private Client menuedItem;

    public ClientAdapter(
        @Nullable Fn.Consumer<Client> onRestart,
        @Nullable Fn.Consumer<Client> onStop,
        @Nullable Fn.Consumer<Client> onDelete) {
        this.onRestart = onRestart;
        this.onStop = onStop;
        this.onDelete = onDelete;
    }

    @Override
    public int getItemCount() { return clients.size(); }

    @Override
    public int getItemViewType(int pos) {
        return ((menuedItem == null) || (!menuedItem.equals(getClient(pos)))) ? CLIENT_VIEW : MENU_VIEW;
    }

    @NonNull
    @Override
    public ViewHolderBase onCreateViewHolder(@NonNull ViewGroup v, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(v.getContext());
        switch (viewType) {
            case CLIENT_VIEW:
                return new ClientViewHolder(RowClientBinding.inflate(inflater, v, false));
            case MENU_VIEW:
                return new MenuViewHolder(RowMenuBinding.inflate(inflater, v, false));
            default:
                throw new IllegalStateException("Unrecognized view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderBase vh, int pos) { vh.setClient(getClient(pos)); }

    public void populate(@Nullable Set<Client> newClients) {
        Log.d(TAG, "populate: " + newClients.size() + " clients, menu @" + menuedItem);

        final List<Client> sortedConnections = new ArrayList<>();

        if ((newClients != null) && (!newClients.isEmpty())) {
            sortedConnections.addAll(newClients);
            Collections.sort(sortedConnections);
        }

        this.clients = sortedConnections;

        recoverMenu();

        notifyDataSetChanged();
    }

    void setTouchHelper(@NonNull RecyclerView clientView) {
        final Context ctxt = clientView.getContext();
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
            new ClientTouchCallback(
                LayoutInflater.from(ctxt).inflate(R.layout.row_menu, clientView, false),
                new ColorDrawable(ctxt.getResources().getColor(R.color.transparent))));
        itemTouchHelper.attachToRecyclerView(clientView);
    }

    void showMenu(int pos) {
        final Client prevMenuedItem = menuedItem;
        menuedItem = getClient(pos);
        if (Objects.equals(prevMenuedItem, menuedItem)) { return; }
        notifyDataSetChanged();
    }

    private void recoverMenu() {
        if (menuedItem == null) { return; }

        final Client menued = menuedItem;
        menuedItem = null;

        for (Client client: clients) {
            if (menued.equals(client)) {
                menuedItem = client;
                return;
            }
        }
    }

    @Nullable
    private Client getClient(int pos) {
        return ((pos < 0) || (pos >= clients.size())) ? null : clients.get(pos);
    }
}
