package comp5216.sydney.edu.au.ourkitchen.ui.login;

import android.util.Log;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * RegisterViewModel is a {@link ViewModel} used for handling data for the registration views
 */
public class RegisterViewModel extends ViewModel {
    private static final String TAG = "RegisterViewModel";
    private final RegisterRepository registerRepository;
    private final MutableLiveData<Boolean> privateProfile = new MutableLiveData<>();
    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    LiveData<User> createdUserLiveData;
    private MutableLiveData<String> username = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();
    private MutableLiveData<String> firstName = new MutableLiveData<>();
    private MutableLiveData<String> lastName = new MutableLiveData<>();
    private MutableLiveData<List<String>> interests = new MutableLiveData<>();


    /**
     * Constructor
     */
    public RegisterViewModel() {
        this.registerRepository = new RegisterRepository();
    }


    /**
     * Getter for the current set value for privacy of the account
     *
     * @return boolean representing private: true, public: false
     */
    public MutableLiveData<Boolean> getPrivateProfile() {
        return privateProfile;
    }

    /**
     * Getter for the current value for the users interests
     *
     * @return mutable live data of a list of strings
     */
    public MutableLiveData<List<String>> getInterests() {
        if (interests == null) {
            interests = new MutableLiveData<>();
        }
        return interests;
    }

    /**
     * Adds a single new interest to the interest mutable data if not already present
     *
     * @param newInterest string value of a new interest to be added
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
     * Removes a single interest from the interests mutable data
     *
     * @param interestToRemove string value of the interest to remove
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
     * Getter for the username data
     *
     * @return username data
     */
    public MutableLiveData<String> getUsername() {
        if (username == null) {
            username = new MutableLiveData<>();
        }
        return username;
    }

    /**
     * Getter for the password data
     *
     * @return password data
     */
    public MutableLiveData<String> getPassword() {
        if (password == null) {
            password = new MutableLiveData<>();
        }
        return password;
    }

    /**
     * Getter for the first name data
     *
     * @return first name data
     */
    public MutableLiveData<String> getFirstName() {
        if (firstName == null) {
            firstName = new MutableLiveData<>();
        }
        return firstName;
    }

    /**
     * Getter for the last name data
     *
     * @return last name data
     */
    public MutableLiveData<String> getLastName() {
        if (lastName == null) {
            lastName = new MutableLiveData<>();
        }
        return lastName;
    }

    /**
     * Creates a new user using the passed in parameters the data currently in the viewModel.
     *
     * @param email     email string
     * @param password  password string
     * @param firstName first name string
     * @param lastName  last name string
     */
    void createUser(String email, String password, String firstName, String lastName) {
        if (interests.getValue() == null) {
            interests.setValue(new ArrayList<>());
        }
        if (privateProfile.getValue() == null) {
            privateProfile.setValue(true);
        }

        createdUserLiveData = registerRepository.FBRegisterWithEmailPassword(email, password,
                firstName, lastName, interests.getValue(), privateProfile.getValue());
    }

    /**
     * Returns the current state of the registration form
     *
     * @return registration form state data
     */
    public MutableLiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    /**
     * registerDataChanged is a method used to notify the viewModel of changes from the views text
     * inputs
     *
     * @param username  string of username
     * @param password  string of password
     * @param firstName string of first name
     * @param lastName  string of last name
     */
    public void registerDataChanged(String username, String password, String firstName,
                                    String lastName) {
        this.username.setValue(username);
        this.password.setValue(password);
        this.firstName.setValue(firstName);
        this.lastName.setValue(lastName);
        if (!isUserNameValid(username)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null,
                    null, null));
        } else if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password,
                    null, null));
        } else if (!isFirstNameValid(firstName)) {
            registerFormState.setValue(new RegisterFormState(null, null,
                    R.string.invalid_firstName, null));
        } else if (!isLastNameValid(lastName)) {
            registerFormState.setValue(new RegisterFormState(null, null, null,
                    R.string.invalid_lastName));
        } else {
            Log.d(TAG, "registerDataChanged: Form is good");
            registerFormState.setValue(new RegisterFormState(true));
        }
    }

    /**
     * A method used to determine if the current username follows a typical email pattern
     *
     * @param username the username to test
     * @return whether the username is valid
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
     * A method used to determine if the current first name is valid
     *
     * @param firstName the string to test
     * @return whether the first name is valid
     */
    private boolean isFirstNameValid(String firstName) {
        return firstName != null && firstName.trim().length() > 2;

    }

    /**
     * A method used to determine if the current last name is valid
     *
     * @param lastName the string to test
     * @return whether the last name is valid
     */
    private boolean isLastNameValid(String lastName) {
        return lastName != null && lastName.trim().length() > 2;
    }


    /**
     * Computes if the password is valid.
     * Must:
     * - have 6 characters minimum
     * - at least one upper case character
     * - at least one lower case chracter
     * - at least one digit
     * - at least one special characters (@#$%,!?+&=+^)
     *
     * @param password The password which is to be validated
     * @return boolean - true if password is valid and false is invalid
     */
    private boolean isPasswordValid(String password) {
        if (password == null) return false;

        // must be 6 or longer, don't test if shorter.
        if (password.trim().length() < 6) {
            return false;
        }

        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%,!?+&=+^]).{6,30}$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}
