package comp5216.sydney.edu.au.ourkitchen.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Util class containing reusable helper functions.
 */
public class Utils {
    /**
     * Helper function to show toast
     *
     * @param mContext current activity context
     * @param message  toast message to display
     */
    public static void showToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper function to hide the virtual onscreen keyboard.
     *
     * @param activity current activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Helper function that recursively initialises all the views so that when clicking outside
     * an edit text div, the keyboard shuts.
     *
     * @param view     parent view containing children
     * @param activity current activity
     */
    public static void setUpCloseKeyboardOnOutsideClick(View view, Activity activity) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                Utils.hideSoftKeyboard(activity);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setUpCloseKeyboardOnOutsideClick(innerView, activity);
            }
        }
    }
}
