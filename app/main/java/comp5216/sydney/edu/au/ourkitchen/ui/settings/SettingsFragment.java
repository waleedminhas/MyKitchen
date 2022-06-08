package comp5216.sydney.edu.au.ourkitchen.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.ui.login.LoginActivity;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;


public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference updatePasswordPreference =
                getPreferenceScreen().findPreference(getString(R.string.update_password));

        if (updatePasswordPreference != null) {
            updatePasswordPreference.setOnPreferenceClickListener(updatePasswordClickHandler());
        }

        Preference logoutPreference =
                getPreferenceScreen().findPreference(getString(R.string.logout));

        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(logoutClickHandler());
        }

        Preference removeAccountPreference =
                getPreferenceScreen().findPreference(getString(R.string.remove_account));

        if (removeAccountPreference != null) {
            removeAccountPreference.setOnPreferenceClickListener(removeAccountClickHandler());
        }

        Preference sendPasswordResetPreference =
                getPreferenceScreen().findPreference(getString(R.string.send_password_reset_email));

        if (sendPasswordResetPreference != null) {
            sendPasswordResetPreference.setOnPreferenceClickListener(sendPasswordResetClickHandler());
        }
    }

    /**
     * Handles the update password functionality when preference is clicked
     *
     * @return An instance of OnPreferenceClickListener
     */
    private OnPreferenceClickListener updatePasswordClickHandler() {
        return preference -> {
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_update_password, null);
            Context activity = getActivity();

            if (activity == null) {
                return false;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.update_password).setMessage(R.string.use_strong_password).setView(dialogView).setPositiveButton(R.string.update, (dialog, which) -> {
            }).setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {
            });

            final AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                EditText oldPassword = dialogView.findViewById(R.id.oldPasswordInput);
                EditText newPassword = dialogView.findViewById(R.id.newPasswordInput);
                EditText newPasswordConfirmation =
                        dialogView.findViewById(R.id.newPasswordConfirmation);
                handleUpdatePassword(oldPassword, newPassword, newPasswordConfirmation, dialog);
            });

            return false;
        };
    }

    /**
     * Handles the logout functionality when preference is clicked
     *
     * @return An instance of OnPreferenceClickListener
     */
    private OnPreferenceClickListener logoutClickHandler() {
        return preference -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return true;
        };
    }

    /**
     * Handles the remove account functionality when preference is clicked
     *
     * @return An instance of OnPreferenceClickListener
     */
    private OnPreferenceClickListener removeAccountClickHandler() {
        return preference -> {
            Context activity = getActivity();

            if (activity == null) {
                return false;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.remove_account).setMessage(R.string.sure_remove_account).setPositiveButton(R.string.yes, (dialog, which) -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    Utils.showToast(activity, getString(R.string.something_went_wrong));
                    return;
                }

                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.showToast(activity, getString(R.string.account_delete_success));
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Utils.showToast(getActivity(), getString(R.string.account_delete_fail));
                    }
                });
            }).setNegativeButton(R.string.no, (dialog, which) -> {
            });

            builder.create().show();
            return false;
        };
    }

    /**
     * Handles the send password reset functionality when preference is clicked
     *
     * @return An instance of OnPreferenceClickListener
     */
    private OnPreferenceClickListener sendPasswordResetClickHandler() {
        return preference -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Context activity = getActivity();

            if (user == null) {
                Utils.showToast(activity, getString(R.string.something_went_wrong));
                return false;
            }

            String emailAddress = user.getEmail();

            if (emailAddress != null) {
                auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.showToast(activity, getString(R.string.reset_pass_mail_sent_success));
                    } else {
                        Utils.showToast(activity, getString(R.string.reset_pass_mail_sent_fail));
                    }
                });
            }
            return false;
        };
    }

    /**
     * Helper function to update password
     */
    private void handleUpdatePassword(EditText oldPassword, EditText newPassword,
                                      EditText newPasswordConfirmation, AlertDialog dialog) {
        String oldPasswordStr = oldPassword.getText().toString();
        Context activity = getActivity();

        if (oldPasswordStr.isEmpty()) {
            Utils.showToast(activity, getString(R.string.old_password_empty));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Utils.showToast(activity, getString(R.string.something_went_wrong));
            return;
        }

        if (user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),
                    oldPassword.getText().toString());
            user.reauthenticateAndRetrieveData(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String newPasswordStr = newPassword.getText().toString();
                    String newPasswordConfirmationStr =
                            newPasswordConfirmation.getText().toString();

                    if (newPasswordStr.equals(newPasswordConfirmationStr) && !newPasswordStr.isEmpty()) {
                        user.updatePassword(newPasswordStr).addOnCompleteListener(t -> {
                            if (task.isSuccessful()) {
                                Utils.showToast(activity,
                                        getString(R.string.password_update_success));
                                oldPassword.setText("");
                                newPassword.setText("");
                                newPasswordConfirmation.setText("");
                                dialog.dismiss();
                            } else {
                                Utils.showToast(activity, getString(R.string.password_update_fail));
                            }
                        });
                    } else {
                        Utils.showToast(activity, getString(R.string.password_mismatch));
                    }
                }
            });
        }
    }
}
