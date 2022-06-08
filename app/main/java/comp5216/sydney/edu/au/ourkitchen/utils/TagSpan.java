package comp5216.sydney.edu.au.ourkitchen.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import comp5216.sydney.edu.au.ourkitchen.R;

/**
 * Defines a custom clickable span tag for the user's posts. When a tag is clicked it takes you to
 * the TagView
 * fragment and displays all matching posts.
 */
public class TagSpan extends ClickableSpan {
    private final String tag;
    private final Context ctx;

    /**
     * Construct a new TagSpan
     *
     * @param tag post tag
     * @param ctx current activity context
     */
    public TagSpan(String tag, Context ctx) {
        this.tag = tag;
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(@NonNull View view) {
        NavController navController = Navigation.findNavController((Activity) ctx,
                R.id.nav_host_fragment);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TAG_VIEW_TAG_KEY, tag);
        navController.navigate(R.id.tagView, bundle);
    }
}
