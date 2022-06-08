package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.FragmentRegisterInterestsBinding;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;

/**
 * A simple {@link Fragment} subclass to enable the user to add interests during registration.
 */
public class RegisterInterests extends Fragment {
    private static final String TAG = "RegisterInterests";

    private RegisterViewModel registerViewModel;
    private FragmentRegisterInterestsBinding binding;

    private ChipGroup interestsChipGroup;
    private AutoCompleteTextView chipInput;
    private Button nextBtn;
    private ImageView registrationProgress1;
    private ImageView registrationProgress3;

    /**
     * Required empty public constructor
     */
    public RegisterInterests() {
    }

    /**
     * Creates a new instance of this fragment
     *
     * @return the created fragment
     */
    public static RegisterInterests newInstance() {
        RegisterInterests fragment = new RegisterInterests();
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
        if (getActivity() != null) {
            registerViewModel = new ViewModelProvider(getActivity()).get(RegisterViewModel.class);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterInterestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        nextBtn = binding.nextBtn;
        interestsChipGroup = binding.interestChipsGroup;
        chipInput = binding.chipInput;
        registrationProgress1 = binding.registrationProgress1;
        registrationProgress3 = binding.registrationProgress3;

        setupDefaultInterestChips();
        setupInputInterestChips();
        setupButtonListeners();

        if (getActivity() != null) {
            Utils.setUpCloseKeyboardOnOutsideClick(binding.getRoot(), getActivity());
        }

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Helper function to initialize click listeners on buttons
     */
    private void setupButtonListeners() {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment_register);
            // Next button navigates to the privacy screen
            nextBtn.setOnClickListener(v -> navController.navigate(R.id.action_registerInterests_to_registerPrivacy));
            registrationProgress1.setOnClickListener(v -> navController.navigateUp());

            registrationProgress3.setOnClickListener(v -> navController.navigate(R.id.action_registerInterests_to_registerPrivacy));
        }
    }

    /**
     * Helper function to determine state of the default interest chips.
     */
    private void setupDefaultInterestChips() {
        int numChips = interestsChipGroup.getChildCount();
        for (int i = 0; i < numChips; i++) {

            Chip chip = (Chip) interestsChipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    registerViewModel.addInterest(compoundButton.getText().toString());
                    Log.d(TAG, "checked: " + compoundButton.getText().toString());
                } else {
                    registerViewModel.removeInterest(compoundButton.getText().toString());
                    Log.d(TAG, "unchecked: " + compoundButton.getText().toString());
                }
            });
        }
    }

    /**
     * Helper function to watch a text input and add additional interest chips.
     */
    private void setupInputInterestChips() {
        TextWatcher inputTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().endsWith(" ") || charSequence.toString().endsWith(",")) {

                    String interest = charSequence.toString().substring(0,
                            charSequence.toString().length() - 1);
                    if (!interest.trim().isEmpty()) {
                        addChipToGroup(interest);
                        chipInput.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        chipInput.addTextChangedListener(inputTextWatcher);
        chipInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                String interest = textView.getText().toString();
                if (!interest.trim().isEmpty()) {
                    addChipToGroup(interest);
                    chipInput.setText("");
                }
            }
            return false;
        });
    }

    /**
     * Adds a new chip to the chip group
     *
     * @param interest String of new interest to add
     */
    private void addChipToGroup(String interest) {
        if (getActivity() != null) {
            Chip chip = new Chip(getActivity());
            chip.setText(interest);
            chip.setCloseIconVisible(true);
            interestsChipGroup.addView(chip);
            registerViewModel.addInterest(interest);
            chip.setOnCloseIconClickListener(v -> {
                interestsChipGroup.removeView(v);
                registerViewModel.removeInterest(interest);
            });
        }
    }
}
