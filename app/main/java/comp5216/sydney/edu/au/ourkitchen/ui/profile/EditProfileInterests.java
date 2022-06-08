package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;

/**
 * EditProfileInterest extends {@link Fragment} and is responsible for editing the users
 * interests.
 */
public class EditProfileInterests extends Fragment {
    private static final String TAG = "EditProfileInterests";
    private ProfileEditViewModel model;
    private ChipGroup chipGroup;
    private List<String> interestsList;
    private Button saveBtn;
    private AutoCompleteTextView chipInput;


    /**
     * Constructor
     */
    public EditProfileInterests() {
        // Required empty public constructor
    }

    /**
     * creates a new instance of EditProfileInterests
     *
     * @return the created instance
     */
    public static EditProfileInterests newInstance() {
        EditProfileInterests fragment = new EditProfileInterests();
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
        // Inflate the layout for this fragment
        if (getActivity() != null) {
            model = new ViewModelProvider(getActivity()).get(ProfileEditViewModel.class);
        }
        return inflater.inflate(R.layout.fragment_edit_profile_interests, container, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chipGroup = view.findViewById(R.id.interest_chips_group_edit);
        chipInput = view.findViewById(R.id.chip_input_edit);
        saveBtn = view.findViewById(R.id.save_edit_button_interests);

        setupButtonListeners();
        setupInterestsObserver();
        setupNewInterestWatchers();

        if (getActivity() != null) {
            Utils.setUpCloseKeyboardOnOutsideClick(view, getActivity());
        }
    }

    /**
     * sets up an observer for the model interests list and adds to the view.
     */
    private void setupInterestsObserver() {
        if (getActivity() != null) {
            model.getInterests().observe(getActivity(), strings -> {
                if (interestsList == null) {
                    interestsList = new ArrayList<>();
                    strings.forEach(this::addChip);
                } else {
                    interestsList = strings;
                }
            });
        }
    }

    /**
     * sets up a text watcher for the interests input and adds chips.
     */
    private void setupNewInterestWatchers() {
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
                        addChip(interest);
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
                    addChip(interest);
                    chipInput.setText("");
                }

            }

            return false;
        });
    }


    /**
     * sets up the on click listeners for this fragment.
     */
    private void setupButtonListeners() {
        saveBtn.setOnClickListener(v -> {
            model.saveEdit();
            if (getActivity() != null) {
                NavController navController = Navigation.findNavController(getActivity(),
                        R.id.nav_host_fragment);
                navController.navigateUp();
            }
        });
    }


    /**
     * Adds a new chip to the chip group and updates the model.
     *
     * @param interestStr interests as string
     */
    private void addChip(String interestStr) {
        if (getActivity() != null) {
            Chip chip = new Chip(getActivity());
            chip.setText(interestStr);
            chip.setCloseIconVisible(true);
            chipGroup.addView(chip);
            model.addInterest(interestStr);
            chip.setOnCloseIconClickListener(v -> {
                chipGroup.removeView(v);
                model.removeInterest(interestStr);
            });
        }
    }
}
