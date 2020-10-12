package com.couchbase.android.listsync;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import com.couchbase.android.listsync.ui.vm.LoginViewModel;


public class LoginActivity extends AppCompatActivity {
    @SuppressWarnings({"WeakerAccess", "NotNullFieldNotInitialized"})
    @NonNull
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NonNull
    private LoginViewModel viewModel;

    private EditText usernameView;
    private EditText passwordView;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidInjection.inject(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);
        if (viewModel.isLoggedIn()) { nextPage(LoginViewModel.STATUS_OK); }

        setContentView(R.layout.activity_login);

        final TextWatcher buttonEnabler = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence sequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) { enableLoginButton(); }
        };

        usernameView = findViewById(R.id.username);
        usernameView.addTextChangedListener(buttonEnabler);
        passwordView = findViewById(R.id.password);
        passwordView.addTextChangedListener(buttonEnabler);

        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> login());
    }

    private void enableLoginButton() {
        loginButton.setEnabled((usernameView.getText().length() > 2) && (passwordView.getText().length() > 2));
    }

    private void login() {
        viewModel.login(usernameView.getText().toString(), passwordView.getText().toString())
            .observe(this, this::nextPage);
    }

    private void nextPage(String status) {
        if (!LoginViewModel.STATUS_OK.equals(status)) {
            Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            return;
        }

        MainActivity.start(this);
        finish();
    }
}
