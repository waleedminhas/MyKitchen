package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.FragmentEditProfileDetailsBinding;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;

/**
 * EditProfileDetails extends {@link Fragment} and enables a user to edit their profile details
 */
public class EditProfileDetails extends Fragment {
    private static final String TAG = "EditProfileDetails";

    private TextInputEditText firstNameEditTxt;
    private TextInputEditText lastNameEditTxt;
    private ProfileEditViewModel model;
    private FragmentEditProfileDetailsBinding binding;

    /**
     * Constructor
     */
    public EditProfileDetails() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of the EditProfileDetails fragment
     *
     * @return the created fragment
     */
    public static EditProfileDetails newInstance() {
        EditProfileDetails fragment = new EditProfileDetails();
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
        if (getActivity() != null) {
            model = new ViewModelProvider(getActivity()).get(ProfileEditViewModel.class);
        }
        binding = FragmentEditProfileDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstNameEditTxt = binding.userFirstNameEdit;
        lastNameEditTxt = binding.userLastNameEdit;
        Button saveBtn = binding.saveEditButton;

        setupInputObservers();
        setupInputWatchers();

        saveBtn.setOnClickListener(v -> saveEdit());

        if (getActivity() != null) {
            Utils.setUpCloseKeyboardOnOutsideClick(binding.getRoot(), getActivity());
        }
    }

    /**
     * setupInputObservers that sync with the viewModel
     */
    private void setupInputObservers() {
        // observe when viewModel updates
        if (getActivity() != null) {
            final Observer<String> nameObserver = newName -> {
                if (firstNameEditTxt.getText() != null && !newName.equals(firstNameEditTxt.getText().toString())) {
                    firstNameEditTxt.setText(newName);
                }
            };

            model.getUserFirstNameEdit().observe(getActivity(), nameObserver);

            final Observer<String> lastNameObserver = s -> {
                if (lastNameEditTxt.getText() != null && !s.equals(lastNameEditTxt.getText().toString())) {
                    lastNameEditTxt.setText(s);
                }
            };
            model.getUserLastNameEdit().observe(getActivity(), lastNameObserver);
        }
    }

    /**
     * setupInputWatchers to watch first and last name inputs and update the
     * viewModel.
     */
    private void setupInputWatchers() {
        // updates viewModel when text changes.
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (firstNameEditTxt.getText() != null) {
                    model.updateNameEdit(firstNameEditTxt.getText().toString());
                }
            }
        };

        TextWatcher lastNameWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (lastNameEditTxt.getText() != null) {
                    model.updateLastNameEdit(lastNameEditTxt.getText().toString());
                }
            }
        };
        lastNameEditTxt.addTextChangedListener(lastNameWatcher);
        firstNameEditTxt.addTextChangedListener(textWatcher);
    }


    /**
     * saveEdit directs the model to save the current information to the user and navigates up.
     */
    private void saveEdit() {
        if (getActivity() != null) {
            model.saveEdit();
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment);
            navController.navigateUp();
        }
    }
}
