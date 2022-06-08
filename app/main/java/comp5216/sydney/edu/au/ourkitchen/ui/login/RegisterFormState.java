package comp5216.sydney.edu.au.ourkitchen.ui.login;

import androidx.annotation.Nullable;

/**
 * RegisterFormState is used to inform the state of the registration form and return string resource
 * ids related to each individual error.
 */
public class RegisterFormState {
    @Nullable
    private final Integer userNameError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer firstNameError;
    @Nullable
    private final Integer lastNameError;
    private final boolean isDataValid;

    /**
     * Constructor
     *
     * @param userNameError  string resource id for username error
     * @param passwordError  string resource id for password error
     * @param firstNameError string resource id for firstName error
     * @param lastNameError  string resource id for lastName error
     */
    RegisterFormState(@Nullable Integer userNameError, @Nullable Integer passwordError,
                      @Nullable Integer firstNameError, @Nullable Integer lastNameError) {
        this.userNameError = userNameError;
        this.passwordError = passwordError;
        this.firstNameError = firstNameError;
        this.lastNameError = lastNameError;
        this.isDataValid = false;
    }

    /**
     * Constructor for validation state
     *
     * @param isValid informs the class if the data is valid or not
     */
    RegisterFormState(boolean isValid) {
        this.userNameError = null;
        this.passwordError = null;
        this.firstNameError = null;
        this.lastNameError = null;
        this.isDataValid = isValid;
    }

    /**
     * Getter for the username error resource string
     *
     * @return string resource id for username error
     */
    @Nullable
    public Integer getUserNameError() {
        return userNameError;
    }

    /**
     * Getter for the password error resource string
     *
     * @return string resource id for password error
     */
    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }


    /**
     * Getter for the first name error resource string
     *
     * @return string resource id for first name error
     */
    @Nullable
    public Integer getFirstNameError() {
        return firstNameError;
    }

    /**
     * Getter for the last name error resource string
     *
     * @return string resource id for last name error
     */
    @Nullable
    public Integer getLastNameError() {
        return lastNameError;
    }

    /**
     * Getter for the validity of the form
     *
     * @return boolean value representing the validity of the form
     */
    public boolean isDataValid() {
        return isDataValid;
    }
}
