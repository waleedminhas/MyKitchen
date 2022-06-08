package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.USERS_COLLECTION;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * ProfileEditViewModel extends {@link ViewModel}
 * Responsible for handling data associated with editing user profile information
 * Associated fragments: EditProfileDetails, EditProfileInterests
 */
public class ProfileEditViewModel extends ViewModel {
    private static final String TAG = "ProfileEditViewModel";

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private DocumentReference userDocRef;
    private MutableLiveData<String> userFirstNameEdit;
    private MutableLiveData<String> userLastNameEdit;
    private MutableLiveData<List<String>> interests;
    private MutableLiveData<List<User>> friends;

    /**
     * Constructor
     * loads the user from firebase.
     */
    public ProfileEditViewModel() {
        getUserFromFirebase();
    }

    /**
     * Getter for friends list data
     *
     * @return friends list data
     */
    public MutableLiveData<List<User>> getFriends() {
        if (friends == null) {
            friends = new MutableLiveData<>();
        }
        return friends;
    }


    /**
     * Getter for the users interest list
     *
     * @return interests list data
     */
    public MutableLiveData<List<String>> getInterests() {
        if (interests == null) {
            interests = new MutableLiveData<>();
        }
        return interests;
    }

    /**
     * Adds a new interest to the interests list
     *
     * @param newInterest new interest string
     */
    public void addInterest(String newInterest) {
        List<String> list = new ArrayList<>();

        if (interests.getValue() != null) {
            list.addAll(interests.getValue());
        }

        if (!list.contains(newInterest)) {
            list.add(newInterest);
        }
        interests.setValue(list);
    }

    /**
     * Removes an interest from the interests list
     *
     * @param interestToRemove an interest to remove
     */
    public void removeInterest(String interestToRemove) {
        List<String> list = new ArrayList<>();
        if (interests.getValue() != null) {
            list.addAll(interests.getValue());
        }

        int index = list.indexOf(interestToRemove);
        if (index != -1) {
            list.remove(index);
        }

        interests.setValue(list);
    }

    /**
     * Adds a friend to this users friend list
     *
     * @param friend friend to add
     */
    public void addFriendToList(User friend) {
        List<User> friendsList = new ArrayList<>();
        if (friends.getValue() != null) {
            friendsList.addAll(friends.getValue());
        }

        boolean exists = friendsList.stream().anyMatch(f -> f.getUid().equals(friend.getUid()));
        if (!exists) {
            friendsList.add(friend);
        }

        friends.setValue(friendsList);
    }

    /**
     * removes all friends from the friends list
     */
    private void removeAllFriends() {
        List<User> empty = new ArrayList<>();
        friends.setValue(empty);
    }

    /**
     * Getter for the user data
     *
     * @return user data
     */
    public MutableLiveData<User> getUser() {
        return user;
    }

    /**
     * Gets the first name data
     *
     * @return first name data
     */
    public MutableLiveData<String> getUserFirstNameEdit() {
        if (userFirstNameEdit == null) {
            userFirstNameEdit = new MutableLiveData<>();
        }
        return userFirstNameEdit;
    }

    /**
     * Gets the last name data
     *
     * @return last name data
     */
    public MutableLiveData<String> getUserLastNameEdit() {
        if (userLastNameEdit == null) {
            userLastNameEdit = new MutableLiveData<>();
        }
        return userLastNameEdit;
    }

    /**
     * Gets the user from firebase
     */
    private void getUserFromFirebase() {
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            userDocRef = mFirestore.collection(USERS_COLLECTION).document(userId);
            userDocRef.addSnapshotListener((value, error) -> {
                if (value != null) {
                    User currentUser = value.toObject(User.class);

                    if (currentUser != null) {
                        user.setValue(currentUser);

                        String firstName = currentUser.getFirstName();
                        String lastName = currentUser.getLastName();
                        if (firstName != null) {
                            getUserFirstNameEdit();
                            userFirstNameEdit.setValue(firstName);
                        }
                        if (lastName != null) {
                            getUserLastNameEdit();
                            userLastNameEdit.setValue(lastName);
                        }

                        List<String> iList = currentUser.getInterests();
                        if (iList != null) {
                            getInterests();
                            interests.setValue(iList);
                        }

                        List<String> friendsIDlist = currentUser.getFriends();
                        if (friendsIDlist != null) {
                            getFriends();
                            syncFriends(friendsIDlist);

                            if (!friendsIDlist.isEmpty()) {
                                // get that user and load into a List of Users

                                for (String friendId : friendsIDlist) {
                                    Log.d(TAG, "friendID: " + friendId);
                                    DocumentReference friendRef =
                                            mFirestore.collection(USERS_COLLECTION).document(friendId);
                                    friendRef.addSnapshotListener((value1, error1) -> {
                                        if (value1 != null) {
                                            User friend = value1.toObject(User.class);
                                            addFriendToList(friend);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * syncs the friend list
     *
     * @param friendsIDlist string list of friend ids
     */
    private void syncFriends(List<String> friendsIDlist) {
        removeAllFriends();

        if (!friendsIDlist.isEmpty()) {
            // get that user and load into a List of Users

            for (String friendId : friendsIDlist) {
                DocumentReference friendRef =
                        mFirestore.collection(USERS_COLLECTION).document(friendId);
                friendRef.addSnapshotListener((value, error) -> {
                    if (value != null) {
                        User friend = value.toObject(User.class);
                        addFriendToList(friend);
                    }
                });
            }
        }
    }

    /**
     * removes a friend from friends list
     *
     * @param user the user to remove
     */
    public void removeFriend(User user) {
        // remove from users friend list and update in firebase

        if (firebaseAuth.getCurrentUser() != null) {
            String loggedInUser = firebaseAuth.getCurrentUser().getUid();
            String removedUser = user.getUid();

            // remove each other from users friend list.
            user.removeFriend(loggedInUser);
            DocumentReference userRef =
                    mFirestore.collection(USERS_COLLECTION).document(removedUser);
            userRef.set(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "removed friend");
                } else {
                    Log.d(TAG, "Something went wrong remove friend");
                }
            });

            // remove from
            DocumentReference loggedInUserRef =
                    mFirestore.collection(USERS_COLLECTION).document(loggedInUser);

            loggedInUserRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot != null) {
                    User loggedInU = documentSnapshot.toObject(User.class);
                    if (loggedInU != null) {
                        loggedInU.removeFriend(removedUser);

                        loggedInUserRef.set(loggedInU).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: removed frind from logged in user");
                                List<User> fList = friends.getValue();
                                if (fList != null) {
                                    fList.removeIf(f -> f.getUid().equals(removedUser));
                                    friends.setValue(fList);
                                }
                            } else {
                                Log.d(TAG, "onComplete: couldn't removed friend from logged in uer");
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * updates the first name data
     *
     * @param newEdit string to replace
     */
    public void updateNameEdit(String newEdit) {
        if (newEdit.equals(userFirstNameEdit.getValue())) {
            return;
        }
        userFirstNameEdit.setValue(newEdit);
    }

    /**
     * updates the last name data
     *
     * @param newEdit string to replace
     */
    public void updateLastNameEdit(String newEdit) {
        if (newEdit.equals(userLastNameEdit.getValue())) {
            return;
        }
        userLastNameEdit.setValue(newEdit);
    }

    /**
     * saves the edited data to the user
     */
    public void saveEdit() {
        if (userDocRef != null && user.getValue() != null) {
            user.getValue().setFirstName(userFirstNameEdit.getValue());
            user.getValue().setLastName(userLastNameEdit.getValue());
            if (interests != null && interests.getValue() != null) {
                user.getValue().setInterests(interests.getValue());
            }
            userDocRef.set(Objects.requireNonNull(user.getValue())).addOnSuccessListener(unused -> Log.d(TAG, "onSuccess: User updated")).addOnFailureListener(e -> Log.d(TAG, "onFailure: User not updated"));
        }
    }
}
