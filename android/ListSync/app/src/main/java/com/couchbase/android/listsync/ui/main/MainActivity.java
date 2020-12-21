package com.couchbase.android.listsync.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.ActivityMainBinding;
import com.couchbase.android.listsync.ui.login.LoginActivity;


public class MainActivity extends AppCompatActivity {
    public static void start(@NonNull Activity activity) {
        final Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        activity.startActivity(intent);
    }

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @NonNull
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private MainViewModel viewModel;

    @SuppressFBWarnings("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ProduceAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.menu_p2p) {
            viewModel.p2p(this);
            return true;
        }

        if (item.getItemId() == R.id.menu_logout) {
            viewModel.logout(this);
            loggedOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        AndroidInjection.inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        final ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        adapter = ProduceAdapter.setup(this, binding.produce, viewModel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!viewModel.loggedIn()) {
            loggedOut();
            return;
        }

        viewModel.getProduce().observe(this, adapter::populate);
    }

    private void loggedOut() {
        LoginActivity.start(this);
        finish();
    }
}
