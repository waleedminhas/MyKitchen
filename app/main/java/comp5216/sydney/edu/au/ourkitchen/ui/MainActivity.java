package comp5216.sydney.edu.au.ourkitchen.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.ui.login.LoginActivity;
import comp5216.sydney.edu.au.ourkitchen.ui.newsfeed.NewsfeedDirections;
import comp5216.sydney.edu.au.ourkitchen.ui.saved.SavedDirections;
import comp5216.sydney.edu.au.ourkitchen.ui.taggedview.TagViewDirections;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

public class MainActivity extends AppCompatActivity {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {

            setContentView(R.layout.main_activity);

            NavHostFragment navHostFragment =
                    (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

                AppBarConfiguration appBarConfiguration =
                        new AppBarConfiguration.Builder(R.id.newsfeed, R.id.profile, R.id.saved,
                                R.id.chat).build();

                NavigationUI.setupActionBarWithNavController(MainActivity.this, navController,
                        appBarConfiguration);
                NavigationUI.setupWithNavController(bottomNavigationView, navController);


                bottomNavigationView.setOnItemSelectedListener(item -> {
                    NavDestination currentDestination = navController.getCurrentDestination();
                    NavDestination createPost = navController.findDestination(R.id.createPost);

                    if (currentDestination == createPost) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.cancel_post)).setMessage(getString(R.string.cancel_post_confirmation)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> NavigationUI.onNavDestinationSelected(item, navController)).setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        });

                        builder.create().show();
                        return false;
                    }
                    return NavigationUI.onNavDestinationSelected(item, navController);
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search_icon) {
            goToSearch();
            return true;
        } else if (item.getItemId() == R.id.find_friends_icon) {
            goToFindFriends();
            return true;
        } else if (item.getItemId() == R.id.search_chat_icon) {
            goToSearchChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to go to find friends
     */
    private void goToFindFriends() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.findFriends);
    }

    /**
     * Function to go to search chat
     */
    private void goToSearchChat() {
        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment);
        navController.navigate(R.id.chat_search);
    }

    /**
     * Function to go to post search
     */
    private void goToSearch() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null) {
            int id = navController.getCurrentDestination().getId();

            NavDirections action = null;
            if (id == R.id.newsfeed) {
                action = NewsfeedDirections.actionNewsfeedToSearch(Constants.NEWSFEED_SEARCH);
                ((NewsfeedDirections.ActionNewsfeedToSearch) action).setSearchType(Constants.NEWSFEED_SEARCH);
            } else if (id == R.id.saved) {
                action = SavedDirections.actionSavedToSearch(Constants.SAVED_SEARCH);
                ((SavedDirections.ActionSavedToSearch) action).setSearchType(Constants.SAVED_SEARCH);
            } else if (id == R.id.tagView) {
                action = TagViewDirections.actionTagViewToSearch(Constants.NEWSFEED_SEARCH);
                ((TagViewDirections.ActionTagViewToSearch) action).setSearchType(Constants.NEWSFEED_SEARCH);
            }

            if (action != null) {
                navController.navigate(action);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null) {
            if (navController.getCurrentDestination().getId() == R.id.createPost) {
                getOnBackPressedDispatcher().onBackPressed();
                return true;
            }
            if (navController.getCurrentDestination().getId() == R.id.profileEdit) {
                getOnBackPressedDispatcher().onBackPressed();
                return true;
            }
        }
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp();
    }
}
