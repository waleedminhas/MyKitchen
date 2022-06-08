package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.FragmentRegisterPrivacyBinding;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.ui.MainActivity;

/**
 * RegisterPrivacy is a {@link Fragment} subclass that enables a user to select a privacy option
 * for their profile and then complete registration for the application.
 */
public class RegisterPrivacy extends Fragment {
    private Button registerBtn;
    private FragmentRegisterPrivacyBinding binding;
    private RegisterViewModel registerViewModel;
    private RadioGroup privacyRadioGroup;

    /**
     * Constructor
     */
    public RegisterPrivacy() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of RegisterPrivacy fragment
     *
     * @return the created fragment
     */
    public static RegisterPrivacy newInstance() {
        RegisterPrivacy fragment = new RegisterPrivacy();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getActivity() != null) {
            registerViewModel = new ViewModelProvider(getActivity()).get(RegisterViewModel.class);
        }
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterPrivacyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        registerBtn = binding.register;
        privacyRadioGroup = binding.regPrivacyRadioGroup;
        setupButtonListeners();

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Helper function to setup button click listeners
     */
    private void setupButtonListeners() {
        registerBtn.setOnClickListener(v -> {
            Boolean isPrivateProfile = isPrivateProfileSelected();
            registerViewModel.getPrivateProfile().setValue(isPrivateProfile);

            String first = registerViewModel.getFirstName().getValue();
            String last = registerViewModel.getLastName().getValue();
            String password = registerViewModel.getPassword().getValue();
            String email = registerViewModel.getUsername().getValue();
            registerViewModel.createUser(email, password, first, last);

            if (getActivity() != null) {
                registerViewModel.createdUserLiveData.observe(getActivity(), user -> {
                    if (user != null) {
                        Toast.makeText(getActivity(), getString(R.string.registration_success_toast), Toast.LENGTH_LONG).show();
                        goToMainActivity(user);
                    }
                });
            }
        });
    }

    /**
     * Helper function to determine if the public or private option is select for account privacy.
     *
     * @return true when private is selected and false when public is selected.
     */
    private boolean isPrivateProfileSelected() {
        int selectedId = privacyRadioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) privacyRadioGroup.findViewById(selectedId);
        return radioButton.getId() != R.id.radioPublicOption;
    }


    /**
     * Starts an intent to the MainActivity.class.
     *
     * @param user User that has been registered and passed to the main activity.
     */
    private void goToMainActivity(User user) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("USER", user);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
