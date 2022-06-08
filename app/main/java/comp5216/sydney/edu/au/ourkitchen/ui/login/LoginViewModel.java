package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * LoginViewModel is a {@link ViewModel} for the login activity.
 * It is responsible for handling the data associated with the login view.
 */
public class LoginViewModel extends ViewModel {
    private final LoginRepository loginRepository;
    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    LiveData<Boolean> isLoading;
    LiveData<User> authenticatedUserLiveData;

    /**
     * Constructor for the LoginViewModel
     */
    public LoginViewModel() {
        this.loginRepository = new LoginRepository();
    }

    /**
     * This method uses the login repository to authenticate the user with an email and password
     *
     * @param email    a string of the existing users email
     * @param password a string of the existing users password
     */
    void signInWithEmail(String email, String password) {
        authenticatedUserLiveData = loginRepository.FBSignInWithEmailPassword(email, password);
        isLoading = loginRepository.getIsLoading();
    }

    /**
     * Getter for the login form state
     *
     * @return mutable live data of the form
     */
    public MutableLiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    /**
     * This method is passed a username and password to determine if those inputs are valid
     *
     * @param username the string input value of the username field
     * @param password the string input value of the password field
     */
    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    /**
     * isUserNameValid determines if the email address entered follows the pattern of a regular
     * email. If it doesn't it will return false.
     *
     * @param username a string of the current username field
     * @return whether the username is valid or not as a boolean
     */
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    /**
     * isPasswordValid determines if the current value of the password string is not empty and
     * longer
     * than 5 characters
     *
     * @param password string value of password
     * @return whether the password satisfies the minimum password requirements.
     */
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
