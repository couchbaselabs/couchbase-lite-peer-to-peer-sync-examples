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
package com.couchbase.android.listsync.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dagger.android.AndroidInjection;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.ui.vm.BaseViewModel;


public abstract class BaseActivity extends AppCompatActivity {
    protected abstract BaseViewModel getViewModel();

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            getViewModel().logout();
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable final Bundle state) {
        AndroidInjection.inject(this);

        super.onCreate(state);
    }
}
