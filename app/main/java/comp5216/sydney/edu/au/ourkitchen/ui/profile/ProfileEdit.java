package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;


/**
 * ProfileEdit extends {@link Fragment}
 */
public class ProfileEdit extends Fragment {
    private static final String TAG = "ProfileEdit";
    RecyclerView recyclerView;
    FriendsListEditAdapter friendsListEditAdapter;
    //Friend profile click listener for friends list
    FriendsListEditAdapter.OnFriendClick onFriendClickListener = userId -> {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment);

            Bundle bundle = new Bundle();
            bundle.putString(Constants.USER_UID, userId);
            navController.navigate(R.id.showProfile, bundle);
        }
    };

    private ProfileEditViewModel model;
    private Button editDetailsBtn;
    private Button editInterestBtn;
    private TextView detailFirstNameTxt;
    private TextView detailLastNameTxt;
    private TextView detailEmailTxt;
    private TextView interestsListTxt;
    private RadioGroup privacyRadioGroup;
    private RadioButton privateRadioOption;
    private RadioButton publicRadioOption;
    private List<User> friendsList;
    FriendsListEditAdapter.OnMoreClick onMoreClickListener = this::showBottomSheet;
    private FirebaseFirestore mFirestore;

    /**
     * Empty public constructor
     */
    public ProfileEdit() {
    }

    /**
     * Function to get new instance of ProfileEdit
     *
     * @return An instance of ProfileEdit
     */
    public static ProfileEdit newInstance() {
        ProfileEdit fragment = new ProfileEdit();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    /**
     * Hide the search option on this page
     *
     * @param menu options menu
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        int[] ids = new int[]{R.id.search_icon, R.id.find_friends_icon};
        MenuItem item;
        for (int i : ids) {
            item = menu.findItem(i);
            if (item != null) {
                item.setVisible(false);
            }
        }
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
        mFirestore = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_profile_edit, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editDetailsBtn = view.findViewById(R.id.edit_details_button);
        editInterestBtn = view.findViewById(R.id.edit_interests_button);
        detailEmailTxt = view.findViewById(R.id.edit_details_email);
        detailFirstNameTxt = view.findViewById(R.id.edit_details_first_name);
        detailLastNameTxt = view.findViewById(R.id.edit_details_last_name);
        interestsListTxt = view.findViewById(R.id.edit_details_interests_list);
        recyclerView = view.findViewById(R.id.friends_recycler_list_edit);
        privacyRadioGroup = view.findViewById(R.id.privacy_radio_group_edit);
        privateRadioOption = view.findViewById(R.id.radioEditPrivateOption);
        publicRadioOption = view.findViewById(R.id.radioEditPublicOption);

        setupButtonListeners();
        setupUserObserver();
        setupRecyclerView();
        setupFriendsObserver();
        setupPrivacyRadio();
    }

    /**
     * launches a dialog to confirm friend deletion
     *
     * @param user user to remove
     */
    private void removeFriendDialog(User user) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getString(R.string.delete_friend_confirmation, user.getFullName()))
                    .setPositiveButton(
                            getString(R.string.yes),
                            (dialogInterface, i) -> {
                                Log.d(TAG, "onClick: removing!!!");
                                model.removeFriend(user);
                                friendsListEditAdapter.notifyDataSetChanged();
                            })
                    .setNegativeButton(
                            getString(R.string.no),
                            (dialogInterface, i) -> Log.d(TAG, "onClick: not removing"));
            builder.create().show();
        }
    }

    /**
     * launches a bottom sheet with options to delete friend.
     *
     * @param userId user id of friend to load into the bottom sheet.
     */
    private void showBottomSheet(String userId) {
        if (getActivity() != null) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
            bottomSheetDialog.setContentView(R.layout.friends_bottom_sheet_edit);

            TextView unfriendTxt = bottomSheetDialog.findViewById(R.id.unfriend_text);
            TextView name = bottomSheetDialog.findViewById(R.id.unfriend_friend_name);
            ImageView icon = bottomSheetDialog.findViewById(R.id.unfriend_icon);


            User user = friendsList.stream().filter(u -> u.getUid().equals(userId)).findFirst().orElse(null);

            if (name != null && unfriendTxt != null && user != null) {
                name.setText(user.getFullName());
                String text = getString(R.string.unfriend_username, user.getFullName());
                unfriendTxt.setText(text);

                unfriendTxt.setOnClickListener(v -> {
                    removeFriendDialog(user);
                    bottomSheetDialog.dismiss();
                });
            }

            if (icon != null) {
                icon.setOnClickListener(v -> {
                    removeFriendDialog(user);
                    bottomSheetDialog.dismiss();
                });
            }

            bottomSheetDialog.show();
        }
    }

    /**
     * Setup observer for the users details
     */
    private void setupUserObserver() {
        if (getActivity() != null) {
            model.getUser().observe(getActivity(), user -> {
                if (user != null) {
                    detailEmailTxt.setText(user.getEmail());
                    detailFirstNameTxt.setText(user.getFirstName());
                    detailLastNameTxt.setText(user.getLastName());
                    interestsListTxt.setText(user.getInterestsAsString());
                }
            });
        }
    }

    /**
     * Setup recycler view for friends list
     */
    private void setupRecyclerView() {
        if (friendsList == null) {
            friendsList = new ArrayList<>();
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsListEditAdapter = new FriendsListEditAdapter(friendsList, onFriendClickListener,
                onMoreClickListener);
        recyclerView.setAdapter(friendsListEditAdapter);
    }

    /**
     * Setup observer for friends list
     */
    private void setupFriendsObserver() {
        if (getActivity() != null) {
            model.getFriends().observe(getActivity(), users -> {
                friendsList.clear();
                users.forEach(f -> {
                    boolean exists =
                            friendsList.stream().anyMatch(friend -> friend.getUid().equals(f.getUid()));
                    if (!exists) {
                        friendsList.add(f);
                    }
                });

                friendsListEditAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Setup for click listeners for this view
     */
    private void setupButtonListeners() {
        if (getActivity() != null) {
            editDetailsBtn.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(getActivity(),
                        R.id.nav_host_fragment);
                navController.navigate(R.id.action_profileEdit_to_editProfileDetails);
            });

            editInterestBtn.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(getActivity(),
                        R.id.nav_host_fragment);
                navController.navigate(R.id.action_profileEdit_to_editProfileInterests);
            });
        }
    }

    /**
     * Initializes privacy radio options, listens for changes and updates firebase accordingly.
     */
    private void setupPrivacyRadio() {
        final boolean[] set = {false};
        if (getActivity() != null) {
            model.getUser().observe(getActivity(), user -> {
                if (user != null) {
                    // Initialize radio on first load.
                    if (!set[0]) {
                        if (user.isPrivateProfile()) {
                            privateRadioOption.setChecked(true);
                            publicRadioOption.setChecked(false);
                        } else {
                            publicRadioOption.setChecked(true);
                            privateRadioOption.setChecked(false);
                        }

                        // Update firebase on change.
                        DocumentReference userDocRef =
                                mFirestore.collection(Constants.USERS_COLLECTION).document(user.getUid());
                        privacyRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
                            boolean isPrivate;
                            switch (checkedId) {
                                case R.id.radioEditPrivateOption: {
                                    isPrivate = true;
                                    break;
                                }
                                case R.id.radioEditPublicOption: {
                                    isPrivate = false;
                                    break;
                                }
                                default:
                                    isPrivate = true;
                            }
                            user.setPrivateProfile(isPrivate);
                            userDocRef.update(Constants.PRIVATE_PROFILE, isPrivate);
                        });
                        set[0] = true;
                    }
                }
            });
        }
    }
}
