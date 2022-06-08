package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import comp5216.sydney.edu.au.ourkitchen.databinding.ActivityRegisterBinding;

/**
 * The RegisterActivity class extends {@link AppCompatActivity} to create a container activity for
 * the fragments related to registering a user.
 * These fragments include: RegisterDetails, RegisterInterests, and RegisterPrivacy.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        comp5216.sydney.edu.au.ourkitchen.databinding.ActivityRegisterBinding binding =
                ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        hideActionBar();
    }

    /**
     * Hides the action bar for this activity
     */
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}
