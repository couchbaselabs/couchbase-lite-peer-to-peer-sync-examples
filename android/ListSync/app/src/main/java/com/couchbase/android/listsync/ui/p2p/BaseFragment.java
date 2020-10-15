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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;


public class BaseFragment extends Fragment {
    public interface VMProvider {
        ViewModelProvider.Factory getViewModelFactory();
    }

    private ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (!(context instanceof VMProvider)) {
            throw new IllegalStateException("Fragment's context is not a VMProvider");
        }

        viewModelFactory = ((VMProvider) context).getViewModelFactory();
    }

    @NonNull
    public <T extends ViewModel> T getViewModel(Class<T> klass) {
        if (viewModelFactory == null) {
            throw new IllegalStateException("getViewModel called before onAttach");
        }

        return ViewModelProviders.of(this, viewModelFactory).get(klass);
    }
}
