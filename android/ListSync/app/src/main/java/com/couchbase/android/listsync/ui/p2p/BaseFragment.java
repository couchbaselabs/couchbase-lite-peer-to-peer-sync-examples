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

import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.couchbase.lite.internal.utils.Fn;


public abstract class BaseFragment extends Fragment {
    public interface VMProvider {
        ViewModelProvider.Factory getViewModelFactory();
    }

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


    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ViewModelProvider.Factory viewModelFactory;

    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof VMProvider)) {
            throw new IllegalStateException("Fragment's context is not a VMProvider");
        }

        viewModelFactory = ((VMProvider) context).getViewModelFactory();
    }

    @NonNull
    public final <T extends ViewModel> T getViewModel(@NonNull Class<T> klass) {
        return ViewModelProviders.of(this, viewModelFactory).get(klass);
    }
}
