package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.FragmentRegisterDetailsBinding;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;


/**
 * RegisterDetails is {@link Fragment} subclass used to gather basic information to register a user.
 */
public class RegisterDetails extends Fragment {

    private RegisterViewModel registerViewModel;
    private FragmentRegisterDetailsBinding binding;
    private EditText emailText;
    private EditText passwordText;
    private EditText firstNameText;
    private EditText lastNameText;
    private Button nextBtn;

    /**
     * Required empty public constructor
     */
    public RegisterDetails() {
    }

    /**
     * Used to create a new instance of RegisterDetails
     *
     * @return the created fragment
     */
    public static RegisterDetails newInstance() {
        RegisterDetails fragment = new RegisterDetails();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            registerViewModel = new ViewModelProvider(getActivity()).get(RegisterViewModel.class);
        }

        emailText = binding.email;
        passwordText = binding.password;
        firstNameText = binding.firstName;
        lastNameText = binding.lastName;
        nextBtn = binding.nextRegisterScreen;

        setupButtonListeners();
        setupTextFieldObservers();

        if (getActivity() != null) {
            Utils.setUpCloseKeyboardOnOutsideClick(binding.getRoot(), getActivity());
        }
    }

    /**
     * Helper function to setup on click listeners
     */
    private void setupButtonListeners() {
        // Navigate to the next stage of registration
        if (getActivity() != null) {
            nextBtn.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(getActivity(),
                        R.id.nav_host_fragment_register);
                navController.navigate(R.id.action_registerDetails_to_registerInterests);
            });
        }
    }

    /**
     * Helper function to setup the text fields and their observables.
     */
    private void setupTextFieldObservers() {
        // Observe the form state and enable button when the data is valid.
        if (getActivity() != null) {
            registerViewModel.getRegisterFormState().observe(getActivity(), registerFormState -> {

                if (registerFormState == null) {
                    return;
                }

                nextBtn.setEnabled(registerFormState.isDataValid());
                if (registerFormState.getUserNameError() != null) {
                    emailText.setError(getString(registerFormState.getUserNameError()));
                }
                if (registerFormState.getPasswordError() != null) {
                    passwordText.setError(getString(registerFormState.getPasswordError()));
                }
                if (registerFormState.getFirstNameError() != null) {
                    firstNameText.setError(getString(registerFormState.getFirstNameError()));
                }
                if (registerFormState.getLastNameError() != null) {
                    lastNameText.setError(getString(registerFormState.getLastNameError()));
                }
            });

            // TextWatcher for the inputs
            TextWatcher afterTextChangedListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // do nothing
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // do nothing
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    registerViewModel.registerDataChanged(emailText.getText().toString(),
                            passwordText.getText().toString(), firstNameText.getText().toString(),
                            lastNameText.getText().toString());
                }
            };

            emailText.addTextChangedListener(afterTextChangedListener);
            passwordText.addTextChangedListener(afterTextChangedListener);
            firstNameText.addTextChangedListener(afterTextChangedListener);
            lastNameText.addTextChangedListener(afterTextChangedListener);
        }
    }
}
