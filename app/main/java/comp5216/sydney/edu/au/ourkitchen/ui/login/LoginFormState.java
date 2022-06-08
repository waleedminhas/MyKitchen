package comp5216.sydney.edu.au.ourkitchen.ui.login;

import androidx.annotation.Nullable;

/**
 * LoginFormState is responsible for managing the input validation on the login form
 */
public class LoginFormState {
    @Nullable
    private final Integer userNameError;
    @Nullable
    private final Integer passwordError;
    private final boolean isDataValid;

    /**
     * Constructor
     *
     * @param userNameError id of string resource with error message
     * @param passwordError id of string resource with error message
     */
    LoginFormState(@Nullable Integer userNameError, @Nullable Integer passwordError) {
        this.userNameError = userNameError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    /**
     * Constructor with valid form state.
     *
     * @param isValid used to indicate a valid or invalid form
     */
    LoginFormState(boolean isValid) {
        this.userNameError = null;
        this.passwordError = null;
        this.isDataValid = isValid;
    }

    /**
     * Gets the resource string error message id
     *
     * @return id of resource string
     */
    @Nullable
    public Integer getUserNameError() {
        return userNameError;
    }

    /**
     * Gets the resource string error message id
     *
     * @return id of resource string
     */
    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    /**
     * Getter for isDataValid
     *
     * @return boolean
     */
    public boolean isDataValid() {
        return isDataValid;
    }
}
