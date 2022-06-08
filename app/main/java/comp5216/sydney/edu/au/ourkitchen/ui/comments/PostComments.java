package comp5216.sydney.edu.au.ourkitchen.ui.comments;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Comment;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.TagSpan;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;
import comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler.CommentListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler.CommentsAdapter;
import comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler.InitScrollingComments;

/**
 * A simple {@link Fragment} subclass for displaying the post comments. Displays all the comments
 * associated with a post.
 */
public class PostComments extends Fragment {
    // Set up list of comments
    private final List<Comment> commentList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private String postId;
    private EditText commentInput;
    private TextView noCommentsToShow;
    private CommentsAdapter commentsAdapter;

    private CollectionReference commentsRef;
    private CollectionReference postsRef;
    private FirebaseUser currentUser;
    private FirebaseFirestore mFirestore;

    // Post info
    private TextView mTags, mModification, mComment, mLink, mPoster;
    private RatingBar mRating;
    private ImageView userImageView, mLikeButton;
    private String currentUserId, userId;
    private Post post;

    private ProgressBar progressBar;

    // Like and unlike tags
    private String liked, notLiked;
    private Button postCommentBtn;
    private RecyclerView commentsRecyclerView;
    private ConstraintLayout postContainer;

    /**
     * Required empty public constructor for fragment
     */
    public PostComments() {
    }

    /**
     * Factory method creates a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostComments.
     */
    public static PostComments newInstance() {
        PostComments fragment = new PostComments();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            postId = getArguments().getString(Constants.POST_ID);
        }
    }

    /**
     * Hide the search option on this page
     *
     * @param menu options menu
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
        // Inflate the layout for this fragment
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        commentsRef = mFirestore.collection(Constants.COMMENT_COLLECTION);
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        return inflater.inflate(R.layout.fragment_post_comments, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commentInput = view.findViewById(R.id.comment_input);
        postCommentBtn = view.findViewById(R.id.post_comment);
        postCommentBtn.setOnClickListener(view1 -> postComment());

        noCommentsToShow = view.findViewById(R.id.no_matching_results);
        progressBar = view.findViewById(R.id.progress_bar);
        showLoadingDialog();
        initComments(view);
        initPost(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        commentsAdapter.clear();
    }

    /**
     * Creates and displays a loading dialog on screen
     */
    private void showLoadingDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Helper function to initialise the view with the information about the post
     *
     * @param itemView fragment view
     */
    private void initPost(View itemView) {
        postContainer = itemView.findViewById(R.id.post);
        mTags = itemView.findViewById(R.id.postHashTags);
        mModification = itemView.findViewById(R.id.modifications);
        mComment = itemView.findViewById(R.id.comments);
        mLink = itemView.findViewById(R.id.hyperlink);
        mRating = itemView.findViewById(R.id.ratingBar);
        userImageView = itemView.findViewById(R.id.posters_image);
        mPoster = itemView.findViewById(R.id.posters_name);
        mLikeButton = itemView.findViewById(R.id.like_image);
        currentUserId = currentUser != null ? currentUser.getUid() : null;

        postsRef.document(postId).get().addOnSuccessListener(documentSnapshot -> {
            post = documentSnapshot.toObject(Post.class);
            if (post != null) {
                mModification.setText(post.getModification());
                mRating.setRating(post.getRating());
                mComment.setText(post.getComment());
                mLink.setText(post.getRecipeUrl());
                userId = post.getUserID();
            }

            setUpTagLinksClickable();
            setUpLikeButton();
            setUpUserInfo();

            closeLoading();
            Utils.setUpCloseKeyboardOnOutsideClick(itemView.findViewById(R.id.display), getActivity());
        });
    }

    /**
     * Helper function to close the loading dialog
     */
    private void closeLoading() {
        progressBar.setVisibility(View.GONE);
        postContainer.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
        commentInput.setVisibility(View.VISIBLE);
        postCommentBtn.setVisibility(View.VISIBLE);

        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(commentsRecyclerView,
                noCommentsToShow);
        commentsAdapter.registerAdapterDataObserver(emptyDataObserver);
    }

    /**
     * Set up the tags to be clickable
     */
    private void setUpTagLinksClickable() {
        if (post.getTags() != null && post.getTags().size() > 0) {
            String allTags = "#" + TextUtils.join(" #", post.getTags());
            SpannableString ss = new SpannableString(allTags);
            int idx = 0;
            for (String tag : post.getTags()) {
                ss.setSpan(new TagSpan(tag, getActivity()), idx, idx + tag.length() + 1,
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
     * Set up user information associated with the post. Populate the information on the view
     */
    private void setUpUserInfo() {
        if (!(userId == null || userId.equals(""))) {
            mFirestore.collection(Constants.USERS_COLLECTION).document(userId).addSnapshotListener((value, error) -> {
                if (value != null) {
                    User user = value.toObject(User.class);
                    if (user != null) {
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        if (!(firstName == null || lastName == null)) {
                            String name = firstName + " " + lastName;
                            mPoster.setText(name);
                        }

                        String profilePicURI = user.getProfilePhoto();
                        if (getContext() != null) {
                            Glide.with(getContext()).load(profilePicURI).into(userImageView);
                        }
                    }
                }
            });
        }
    }

    /**
     * Helper function to set up the like button functionality
     */
    private void setUpLikeButton() {
        if (getActivity() != null) {
            liked = getActivity().getResources().getString(R.string.post_liked);
            notLiked = getActivity().getResources().getString(R.string.post_not_liked);

            mLikeButton.setOnClickListener((View view) -> {

                NavController navController = Navigation.findNavController(getActivity(),
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(getString(R.string.unsave_post)).setMessage(getString(R.string.unsave_post_confirmation_message)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> unlikePost(currentUserId, post)).setNegativeButton(getString(R.string.no), (dialogInterface, i) -> { /* Do nothing */ });
                        builder.create().show();
                    } else {
                        unlikePost(currentUserId, post);
                    }
                }
            });
        }
    }

    /**
     * Function called when user clicks like button to like post
     *
     * @param currentUserId current user id
     * @param post          post to like
     */
    private void likePost(String currentUserId, Post post) {
        if (getActivity() != null) {
            mLikeButton.setImageDrawable(ResourcesCompat.getDrawable(getActivity().getResources()
                    , R.drawable.ic_baseline_favorite_24, null));
            mLikeButton.setTag(liked);

            // Update in firebase
            mFirestore.collection(Constants.USERS_COLLECTION).document(currentUserId).update(
                    "likedPosts", FieldValue.arrayUnion(post.getId()));
            mFirestore.collection(Constants.POSTS_COLLECTION).document(post.getId()).update(
                    "likedBy", FieldValue.arrayUnion(currentUserId));
        }
    }

    /**
     * Function called when user clicks like button to unlike post
     *
     * @param currentUserId current user id
     * @param post          post to unlike
     */
    private void unlikePost(String currentUserId, Post post) {
        if (getActivity() != null) {
            mLikeButton.setImageDrawable(ResourcesCompat.getDrawable(getActivity().getResources()
                    , R.drawable.ic_baseline_favorite_border_24, null));
            mLikeButton.setTag(notLiked);

            // Update in firebase
            mFirestore.collection(Constants.USERS_COLLECTION).document(currentUserId).update(
                    "likedPosts", FieldValue.arrayRemove(post.getId()));
            mFirestore.collection(Constants.POSTS_COLLECTION).document(post.getId()).update(
                    "likedBy", FieldValue.arrayRemove(currentUserId));
        }
    }

    /**
     * Helper function for when the post comment button is clicked. Photo is uploaded and the
     * keyboard is hidden
     */
    private void postComment() {
        // Hide keyboard when comment button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        new Thread(() -> {
            String id = commentsRef.document().getId();
            Comment comment = new Comment(id, postId, currentUser.getUid(),
                    new Timestamp(new Date()), commentInput.getText().toString());
            postsRef.document(postId).update("comments", FieldValue.arrayUnion(id));
            commentsRef.document(id).set(comment);
            commentInput.setText("");
        }).start();
    }

    /**
     * Helper function initialise the recycler view of comments
     *
     * @param view fragment view
     */
    private void initComments(View view) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        commentsRecyclerView = view.findViewById(R.id.comment_recycler_view);
        commentsRecyclerView.setLayoutManager(layoutManager);

        commentsAdapter = new CommentsAdapter(commentList, getActivity());
        commentsRecyclerView.setAdapter(commentsAdapter);

        CommentListViewModel commentsListViewModel =
                new ViewModelProvider(this).get(CommentListViewModel.class);

        Query query =
                commentsRef.whereEqualTo("postId", postId).orderBy(Constants.TIMESTAMP_FIELD,
                        Query.Direction.DESCENDING).limit(Constants.LIMIT);

        InitScrollingComments.getComments(query, true, commentsListViewModel, getActivity(),
                commentsAdapter, commentList);
        InitScrollingComments.initRecyclerViewOnScrollListener(isScrolling, query,
                commentsListViewModel, getActivity(), commentsAdapter, commentList,
                commentsRecyclerView, layoutManager);
    }
}
