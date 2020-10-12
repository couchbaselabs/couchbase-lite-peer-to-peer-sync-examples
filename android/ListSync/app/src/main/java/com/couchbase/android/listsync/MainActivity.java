package com.couchbase.android.listsync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.couchbase.android.listsync.ui.BaseActivity;
import com.couchbase.android.listsync.ui.vm.BaseViewModel;
import com.couchbase.android.listsync.ui.vm.MainViewModel;


public class MainActivity extends BaseActivity {
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

    @Override
    protected BaseViewModel getViewModel() { return viewModel; }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);

        setContentView(R.layout.activity_main);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration
            = new AppBarConfiguration.Builder(R.id.navigation_active, R.id.navigation_passive).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
