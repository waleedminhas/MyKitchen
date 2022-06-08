package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.POSTS_COLLECTION;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.TagSpan;

/**
 * Adapter for list of posts, will display the posts as a scrollable list.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {
    private final List<Post> postList;
    private final boolean hasUserClickListener;
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private final FragmentActivity activity;
    private Context ctx;

    /**
     * Constructor for post adapter.
     *
     * @param postList    list of posts to display
     * @param hasListener true if clicking on the associated post's user will link to their page,
     *                    false if otherwise
     * @param activity    current activity
     */
    public PostsAdapter(List<Post> postList, Boolean hasListener, FragmentActivity activity) {
        this.postList = postList;
        this.activity = activity;
        this.hasUserClickListener = hasListener;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.newsfeed_post,
                parent, false);
        ctx = parent.getContext();
        return new PostViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bindPost(post);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return postList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Removes all posts from the list of posts.
     */
    public void clear() {
        this.postList.clear();
        notifyDataSetChanged();
    }

    /**
     * The view holder for each individual post in the list.
     */
    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView mTags, mModification, mComment, mLink, mPoster;
        RatingBar mRating;
        ImageView mImageView, userImageView, mLikeButton, mDeleteButton;
        String currentUserId, userId;
        Post post;
        Button commentBtn;
        String liked;
        String notLiked;

        /**
         * Constructor for post view holder
         *
         * @param itemView item view
         */
        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mTags = itemView.findViewById(R.id.postHashTags);
            mModification = itemView.findViewById(R.id.modifications);
            mComment = itemView.findViewById(R.id.comments);
            mLink = itemView.findViewById(R.id.hyperlink);
            mRating = itemView.findViewById(R.id.ratingBar);
            mImageView = itemView.findViewById(R.id.postImage);
            userImageView = itemView.findViewById(R.id.posters_image);
            mPoster = itemView.findViewById(R.id.posters_name);
            mLikeButton = itemView.findViewById(R.id.like_image);
            mDeleteButton = itemView.findViewById(R.id.delete);
            currentUserId = mFirebaseAuth.getCurrentUser() != null ?
                    mFirebaseAuth.getCurrentUser().getUid() : null;
            commentBtn = itemView.findViewById(R.id.comment_action);
            liked = ctx.getResources().getString(R.string.post_liked);
            notLiked = ctx.getResources().getString(R.string.post_not_liked);
        }

        /**
         * Binds the post to the recycler view
         *
         * @param post post to bind
         */
        void bindPost(@NonNull Post post) {
            this.post = post;

            populatePostInfo();

            commentBtn.setOnClickListener(view -> goToComments());

            setUpTagLinksClickable();
            setUpDeleteBtn();
            setUpLikeBtn();
            addUserInfoToPost();
            addPostsPhoto();
        }

        /**
         * Helper function to populate the post info on the view
         */
        private void populatePostInfo() {
            mModification.setText(post.getModification());
            mRating.setRating(post.getRating());
            mComment.setText(post.getComment());
            mLink.setText(post.getRecipeUrl());
            userId = post.getUserID();
        }

        /**
         * Set up the delete button visibility and on click listener
         */
        private void setUpDeleteBtn() {
            if (userId.equals(currentUserId)) {
                mDeleteButton.setVisibility(View.VISIBLE);
                mDeleteButton.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(ctx.getResources().getString(R.string.delete_post))
                            .setMessage(ctx.getResources().getString(R.string.delete_post_confirmation_message))
                            .setPositiveButton(
                                    ctx.getResources().getString(R.string.yes),
                                    (dialogInterface, i) -> deletePost(post.getId()))
                            .setNegativeButton(
                                    ctx.getResources().getString(R.string.no),
                                    (dialogInterface, i) -> {
                                    });
                    builder.create().show();
                });
            } else {
                mDeleteButton.setVisibility(View.GONE);
            }
        }

        /**
         * Set up the like button on click listener
         */
        private void setUpLikeBtn() {
            mLikeButton.setOnClickListener((View view) -> {
                NavController navController = Navigation.findNavController(activity,
                        R.id.nav_host_fragment);
                boolean needToConfirm = false;
                if (navController.getCurrentDestination() != null) {
                    int id = navController.getCurrentDestination().getId();
                    if (id == R.id.saved) {
                        needToConfirm = true;
                    }
                }
                if (mLikeButton.getTag().toString().equals(notLiked)) {
                    likePost(currentUserId, post);

                } else {
                    if (needToConfirm) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(ctx.getResources().getString(R.string.unsave_post))
                                .setMessage(
                                        ctx.getResources().getString(
                                                R.string.unsave_post_confirmation_message
                                        )
                                )
                                .setPositiveButton(
                                        ctx.getResources().getString(R.string.yes),
                                        (dialogInterface, i) -> unlikePost(currentUserId, post))
                                .setNegativeButton(
                                        ctx.getResources().getString(R.string.no),
                                        (dialogInterface, i) -> { /* Do nothing */ });
                        builder.create().show();
                    } else {
                        unlikePost(currentUserId, post);
                    }
                }
            });
        }

        /**
         * If the there is an associated user for the post, show their name and profile picture
         * if they have one.
         */
        private void addUserInfoToPost() {
            if (!(userId == null || userId.equals(""))) {
                if (hasUserClickListener && activity != null) {
                    userImageView.setOnClickListener((view) -> {
                        NavController navController = Navigation.findNavController(activity,
                                R.id.nav_host_fragment);

                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.USER_UID, userId);
                        navController.navigate(R.id.showProfile, bundle);
                    });
                }

                mFirestore.collection(Constants.USERS_COLLECTION).document(userId).addSnapshotListener((user, error) -> {
                    if (user != null) {
                        User postUser = user.toObject(User.class);
                        if (postUser != null) {
                            String firstName = postUser.getFirstName();
                            String lastName = postUser.getLastName();
                            if (!(firstName == null || lastName == null)) {
                                String name = firstName + " " + lastName;
                                mPoster.setText(name);
                            }
                            String profilePicURI = postUser.getProfilePhoto();
                            if (profilePicURI != null && !profilePicURI.equals("")) {
                                Glide.with(ctx).load(profilePicURI).into(userImageView);
                            }
                        }
                    }
                });
                if (post.getLikedBy() != null && post.getLikedBy().contains(currentUserId)) {
                    mLikeButton.setImageDrawable(ResourcesCompat.getDrawable(ctx.getResources(),
                            R.drawable.ic_baseline_favorite_24, null));
                    mLikeButton.setTag(liked);
                }
            }
        }

        /**
         * If there is a photo associated with the post, add it to the view.
         */
        private void addPostsPhoto() {
            if (post.getLinkToPhoto() != null) {
                Glide.with(ctx).load(post.getLinkToPhoto()).into(mImageView);
            }
        }

        /**
         * Function called when the user clicks on the comment button.
         */
        private void goToComments() {
            NavController navController = Navigation.findNavController((Activity) ctx,
                    R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.POST_ID, post.getId());
            navController.navigate(R.id.postComments, bundle);
        }

        /**
         * Sets up the tags to be clickable, when clicked it will show a page with all the posts
         * that match the clicked tag.
         */
        private void setUpTagLinksClickable() {
            if (post.getTags() != null && post.getTags().size() > 0) {
                String allTags = "#" + TextUtils.join(" #", post.getTags());
                SpannableString ss = new SpannableString(allTags);
                int idx = 0;
                for (String tag : post.getTags()) {
                    ss.setSpan(new TagSpan(tag, ctx), idx, idx + tag.length() + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    idx += 2 + tag.length();
                }
                mTags.setText(ss);
                mTags.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                mTags.setVisibility(View.GONE);
            }
        }

        /**
         * Delete posts from Firestore.
         *
         * @param id post id
         */
        private void deletePost(String id) {
            mFirestore.collection(POSTS_COLLECTION).document(id).delete();
        }

        /**
         * Likes the selected post.
         *
         * @param currentUserId the id of the user liking the post
         * @param post          post they have just liked
         */
        private void likePost(String currentUserId, Post post) {
            mLikeButton.setImageDrawable(ResourcesCompat.getDrawable(ctx.getResources(),
                    R.drawable.ic_baseline_favorite_24, null));
            mLikeButton.setTag(liked);

            // Update in firebase
            mFirestore.collection(Constants.USERS_COLLECTION).document(currentUserId).update(Constants.USER_LIKED_POSTS_FIELD, FieldValue.arrayUnion(post.getId()));
            mFirestore.collection(Constants.POSTS_COLLECTION).document(post.getId()).update(Constants.POST_LIKED_BY_FIELD, FieldValue.arrayUnion(currentUserId));
        }

        /**
         * Unlikes the selected post.
         *
         * @param currentUserId the id of the user unliking the post
         * @param post          post they have just unliked
         */
        private void unlikePost(String currentUserId, Post post) {
            mLikeButton.setImageDrawable(ResourcesCompat.getDrawable(ctx.getResources(),
                    R.drawable.ic_baseline_favorite_border_24, null));
            mLikeButton.setTag(notLiked);

            // Update in firebase
            mFirestore.collection(Constants.USERS_COLLECTION).document(currentUserId).update(Constants.USER_LIKED_POSTS_FIELD, FieldValue.arrayRemove(post.getId()));
            mFirestore.collection(Constants.POSTS_COLLECTION).document(post.getId()).update(Constants.POST_LIKED_BY_FIELD, FieldValue.arrayRemove(currentUserId));
        }
    }
}
