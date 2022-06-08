package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.ActivityLoginBinding;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.ui.MainActivity;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;


/**
 * Login Activity is responsible for authentication of users.
 */
public class LoginActivity extends AppCompatActivity {

    MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    private LoginViewModel loginViewModel;
    private Button launchRegisterBtn;
    private EditText emailText;
    private EditText passwordText;
    private Button loginBtn;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideActionBar();
        // bind and inflate the view
        comp5216.sydney.edu.au.ourkitchen.databinding.ActivityLoginBinding binding =
                ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup view model
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // view binding
        emailText = binding.email;
        passwordText = binding.password;
        loginBtn = (Button) binding.login;
        launchRegisterBtn = binding.launchRegisterBtn;

        setupButtonListeners();
        setupLoginForm();


        Utils.setUpCloseKeyboardOnOutsideClick(binding.getRoot(), LoginActivity.this);


    }


    /**
     * Hides the action bar for this activity
     */
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }


    /**
     * Helper function to setup on click listeners for this activity
     */
    private void setupButtonListeners() {
        // initiates intent to register activity
        launchRegisterBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // login button
        loginBtn.setOnClickListener(v -> {
            // attempt login
            loginViewModel.signInWithEmail(emailText.getText().toString(),
                    passwordText.getText().toString());
            // await results
            loginViewModel.isLoading.observe(this, loading -> {
                if (!loading && loginSuccess != null && !loginSuccess.getValue()) {
                    Toast.makeText(this, getString(R.string.login_failure_toast_message), Toast.LENGTH_SHORT).show();
                }
            });
            loginViewModel.authenticatedUserLiveData.observe(this, user -> {
                if (user != null) {
                    loginSuccess.setValue(true);
                    Toast.makeText(this, getString(R.string.login_success_toast_message), Toast.LENGTH_LONG).show();
                    goToMainActivity(user);
                }
            });
        });

    }


    /**
     * Helper function that observes the forms texts field and notifies the viewModel of changes
     * for validation purposes.
     */
    private void setupLoginForm() {
        // Observer the form state and enable button when valid data.
        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginBtn.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUserNameError() != null) {
                emailText.setError(getString(loginFormState.getUserNameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // tell the loginviewmodel the data has changed for validation
                loginViewModel.loginDataChanged(emailText.getText().toString(),
                        passwordText.getText().toString());
            }
        };

        emailText.addTextChangedListener(afterTextChangedListener);
        passwordText.addTextChangedListener(afterTextChangedListener);
    }

    /**
     * Launches an Intent to the MainActivity class
     *
     * @param user User that has been authenticated
     */
    private void goToMainActivity(User user) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER", user);
        startActivity(intent);
        finish();
    }
}
