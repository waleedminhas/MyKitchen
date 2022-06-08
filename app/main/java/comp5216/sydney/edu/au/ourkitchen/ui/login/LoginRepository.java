package comp5216.sydney.edu.au.ourkitchen.ui.login;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * LoginRepository is responsible for logging in a user utilizing firebase authentication and
 * mutable live data for observation
 */
public class LoginRepository {
    private final static String TAG = "LoginRepo";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    /**
     * Returns if the login repository is currently in a loading state whilst attempting login.
     *
     * @return boolean
     */
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * FBSignInWithEmailPassword enables a user to login using an email and a password.
     * It utilizes firebase and has an observable authenticatedUser that will be set when successful
     *
     * @param email    email address of user
     * @param password password of user
     * @return live data of authenticated user
     */
    MutableLiveData<User> FBSignInWithEmailPassword(String email, String password) {
        MutableLiveData<User> authenticatedUserLiveData = new MutableLiveData<>();
        isLoading.setValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String email1 = firebaseUser.getEmail();
                    User user = new User(uid, email1);
                    authenticatedUserLiveData.setValue(user);
                    isLoading.setValue(false);
                }
            } else {
                isLoading.setValue(false);
            }
        });
        return authenticatedUserLiveData;
    }
}
