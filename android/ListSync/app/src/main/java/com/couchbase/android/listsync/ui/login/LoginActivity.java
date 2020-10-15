package com.couchbase.android.listsync.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import com.couchbase.android.listsync.databinding.ActivityLoginBinding;
import com.couchbase.android.listsync.ui.main.MainActivity;


public class LoginActivity extends AppCompatActivity {
    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @NonNull
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private LoginViewModel viewModel;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidInjection.inject(this);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
        if (viewModel.isLoggedIn()) { nextPage(LoginViewModel.STATUS_OK); }

        binding = ActivityLoginBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());

        if (binding.password.getText().length() > 0) { return; }
        final TextWatcher buttonEnabler = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { enableLoginButton(); }
        };

        binding.username.addTextChangedListener(buttonEnabler);
        binding.password.addTextChangedListener(buttonEnabler);

        binding.login.setOnClickListener(v -> login());
    }

    private void enableLoginButton() {
        binding.login.setEnabled(
            (binding.username.getText().length() > 2) && (binding.password.getText().length() > 2));
    }

    private void login() {
        viewModel.login(binding.username.getText().toString(), binding.password.getText().toString())
            .observe(this, this::nextPage);
    }

    private void nextPage(@NonNull String status) {
        if (!LoginViewModel.STATUS_OK.equals(status)) {
            Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            return;
        }

        MainActivity.start(this);
        finish();
    }
}
