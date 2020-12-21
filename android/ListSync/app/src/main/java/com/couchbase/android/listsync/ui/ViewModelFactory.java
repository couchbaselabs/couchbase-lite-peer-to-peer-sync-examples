//
// Copyright (c) 2019 Couchbase, Inc All rights reserved.
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
package com.couchbase.android.listsync.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;


@SuppressWarnings("WeakerAccess")
public class ViewModelFactory implements ViewModelProvider.Factory {
    @NonNull
    private final Map<Class<? extends ViewModel>, Provider<ViewModel>> providers;

    @Inject
    public ViewModelFactory(@NonNull Map<Class<? extends ViewModel>, Provider<ViewModel>> providers) {
        this.providers = providers;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
        Provider<ViewModel> modelProvider = providers.get(modelClass);
        if (modelProvider == null) {
            for (Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry : providers.entrySet()) {
                if (modelClass.isAssignableFrom(entry.getKey())) {
                    modelProvider = entry.getValue();
                    break;
                }
            }
        }

        if (modelProvider == null) {
            throw new IllegalArgumentException("Unknown model class: " + modelClass);
        }

        return (T) modelProvider.get();
    }
}
