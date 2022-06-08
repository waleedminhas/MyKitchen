package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * RegisterRepository is responsible for registering a new user with firebase and properly adding
 * additional data gained through the onboarding process.
 */
public class RegisterRepository {

    private final static String TAG = "RegisterRepo";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();


    /**
     * Registers a new user with the required information. The view model should pass the parameters
     * so the user is created with the correct data.
     *
     * @param email            the email of the user
     * @param password         the password of the user
     * @param firstName        the first name of the user
     * @param lastName         the last name of the user
     * @param interests        a string list of interests for the user
     * @param isPrivateProfile a boolean value representing the privacy of the account
     * @return the created user as mutable live data so that can be observed for changes.
     */
    MutableLiveData<User> FBRegisterWithEmailPassword(String email, String password,
                                                      String firstName, String lastName,
                                                      List<String> interests,
                                                      Boolean isPrivateProfile) {
        MutableLiveData<User> createdUserLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String email1 = firebaseUser.getEmail();

                    User user = new User(uid, email1, firstName, lastName);
                    user.setInterests(interests);
                    user.setPrivateProfile(isPrivateProfile);
                    addUserToFirestore(user);
                    createdUserLiveData.setValue(user);
                }
            } else {
                Log.e(TAG, "error signing up");
            }
        });
        return createdUserLiveData;
    }

    /**
     * addUserToFirebase creates the new user in the User collection in Firebase.
     *
     * @param user the user to be added to the database.
     */
    private void addUserToFirestore(User user) {

        DocumentReference documentReference =
                mFirestore.collection(Constants.USERS_COLLECTION).document(user.getUid());

        documentReference.set(user).addOnSuccessListener(unused -> Log.d(TAG, "onSuccess: user success")).addOnFailureListener(e -> Log.d(TAG, "onFailure: user not created in firestore"));
    }
}
