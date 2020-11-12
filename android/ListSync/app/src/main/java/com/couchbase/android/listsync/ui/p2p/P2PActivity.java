package com.couchbase.android.listsync.ui.p2p;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import dagger.android.AndroidInjection;

import com.couchbase.android.listsync.R;
import com.couchbase.android.listsync.databinding.ActivityP2pBinding;


public class P2PActivity extends AppCompatActivity implements BaseFragment.VMProvider {
    public static void start(@NonNull Activity activity) {
        final Intent intent = new Intent(activity, P2PActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        activity.startActivity(intent);
    }

    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @NonNull
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @NonNull
    public ViewModelProvider.Factory getViewModelFactory() { return viewModelFactory; }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        AndroidInjection.inject(this);

        ActivityP2pBinding binding = ActivityP2pBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration
            = new AppBarConfiguration.Builder(
            R.id.nav_active,
            R.id.nav_passive)
            .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
