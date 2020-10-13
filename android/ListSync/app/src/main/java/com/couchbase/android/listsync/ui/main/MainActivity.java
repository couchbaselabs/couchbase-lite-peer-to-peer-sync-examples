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

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        activity.startActivity(intent);
    }

    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @NonNull
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private MainViewModel viewModel;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ActivityMainBinding binding;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private InSeasonAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            viewModel.logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        AndroidInjection.inject(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        adapter = InSeasonAdapter.setup(this, binding.inSeason);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getInSeason().observe(this, adapter::populate);
    }
}
