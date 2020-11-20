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
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;

import java.util.ArrayList;
import java.util.List;

import com.couchbase.lite.internal.utils.Fn;


public abstract class P2PFragment extends Fragment {
    public static class OneShotObserver<T> implements Observer<T> {
        private final LiveData<T> liveData;
        private final Fn.Consumer<T> consumer;

        public OneShotObserver(@NonNull LiveData<T> liveData, @NonNull Fn.Consumer<T> consumer) {
            this.liveData = liveData;
            this.consumer = consumer;
        }

        @Override
        public void onChanged(T data) {
            consumer.accept(data);
            liveData.removeObserver(this);
        }
    }

    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        if (!(context instanceof P2PActivity)) {
            throw new IllegalStateException("Fragment's context is not a P2PActivity");
        }
        super.onAttach(context);
    }

    @NonNull
    public final <T extends ViewModel> T getViewModel(@NonNull Class<T> klass) {
        return ViewModelProviders.of(this, ((P2PActivity) getActivity()).getViewModelFactory()).get(klass);
    }

    protected final List<String> checkPerms(String[] requiredPerms) {
        final Activity act = getActivity();
        final List<String> perms = new ArrayList<>();
        for (String perm: requiredPerms) {
            if (ActivityCompat.checkSelfPermission(act, perm) != PackageManager.PERMISSION_GRANTED) { perms.add(perm); }
        }
        return perms;
    }

    protected final void navigate(int destinationId) {
        ((P2PActivity) getActivity()).getNavController().navigate(destinationId);
    }

    protected final void navigate(NavDirections direction) {
        ((P2PActivity) getActivity()).getNavController().navigate(direction);
    }
}
