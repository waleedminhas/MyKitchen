package comp5216.sydney.edu.au.ourkitchen.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import comp5216.sydney.edu.au.ourkitchen.R;

/**
 * A simple {@link Fragment} subclass of Settings fragment. Enables users to customise settings.
 */
public class Settings extends Fragment {
    public Settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment settings.
     */
    public static Settings newInstance() {
        Settings fragment = new Settings();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On create method when activity is instantiated
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new SettingsFragment())
                .commit();
    }

    /**
     * Hide the search option on this page
     *
     * @param menu menu
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
