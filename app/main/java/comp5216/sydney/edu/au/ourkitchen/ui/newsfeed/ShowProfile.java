package comp5216.sydney.edu.au.ourkitchen.ui.newsfeed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.databinding.FragmentShowProfileBinding;
import comp5216.sydney.edu.au.ourkitchen.model.FriendRequest;
import comp5216.sydney.edu.au.ourkitchen.model.FriendRequestStatus;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * ShowProfile is a {@link Fragment} used to show a users profile.
 */
public class ShowProfile extends Fragment {
    private static final String TAG = "ShowProfile";
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    CollectionReference postsRef;
    String loggedInUserId;
    ListenerRegistration userListener;
    private FragmentShowProfileBinding binding;
    private FirebaseFirestore mFirestore;
    private String profileUserId;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Query currentQuery;
    private LinearLayout noPostsToShow;
    private LinearLayoutManager layoutManager;
    private TextView emailTxt;
    private TextView firstNameTxt;
    private TextView lastNameTxt;
    private ImageView profileImage;
    private Button addFriendBtn;
    private TextView friendsTxt;

    /**
     * Required empty public constructor
     */
    public ShowProfile() {
    }

    /**
     * Creates a new instance of the ShowProfile fragment
     *
     * @return A new instance of fragment ShowProfile.
     */
    public static ShowProfile newInstance() {
        ShowProfile fragment = new ShowProfile();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profileUserId = getArguments().getString(Constants.USER_UID);
        }
    }

    /**
     * Hide the search option on this page
     *
     * @param menu menu
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.search_icon);
        if (item != null) {
            item.setVisible(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        loggedInUserId =
                firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() :
                        null;
        binding = FragmentShowProfileBinding.inflate(inflater, container, false);

        if (profileUserId == null) {
            profileUserId = ShowProfileArgs.fromBundle(getArguments()).getUserId();
        }
        return binding.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emailTxt = binding.userEmail;
        firstNameTxt = binding.userFirstName;
        lastNameTxt = binding.userLastName;
        profileImage = binding.userImage;
        addFriendBtn = binding.addFriendBtn;
        friendsTxt = binding.alreadyFriendsTxt;
        noPostsToShow = binding.noMatchingResults;

        setupProfileInformation();
        setupButtonListeners();

        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * Helper function to setup button listeners
     */
    private void setupButtonListeners() {
        addFriendBtn.setOnClickListener(v -> addFriend());
    }

    /**
     * Helper function that gets the users profile information from Firebase
     * and sets the appropriate fields.
     */
    private void setupProfileInformation() {
        emailTxt.setText(profileUserId);

        DocumentReference documentReference =
                mFirestore.collection(Constants.USERS_COLLECTION).document(profileUserId);

        userListener = documentReference.addSnapshotListener((value, error) -> {
            if (value != null) {

                User user = value.toObject(User.class);
                if (user != null) {
                    emailTxt = binding.userEmail;
                    firstNameTxt = binding.userFirstName;
                    lastNameTxt = binding.userLastName;

                    firstNameTxt.setText(user.getFirstName());
                    lastNameTxt.setText(user.getLastName());
                    emailTxt.setText(user.getEmail());

                    setUpAddFriendBtn(user);

                    handlePrivacyOfPosts(user);

                    setUpProfileImage(user);
                }
            }

        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        userListener.remove();
        super.onDetach();
    }

    /**
     * Helper function to setup the users profile image
     *
     * @param user the user for the image
     */
    private void setUpProfileImage(User user) {
        String profilePicURI = user.getProfilePhoto();
        if (getActivity() != null && profilePicURI != null && !profilePicURI.equals("")) {
            Glide.with(getActivity().getApplicationContext()).load(profilePicURI).into(profileImage);
        }
    }

    /**
     * Helper function to setup the visibility of the add friend button
     *
     * @param user User
     */
    private void setUpAddFriendBtn(User user) {
        List<String> friends = user.getFriends();
        if (user.getUid().equals(loggedInUserId)) {
            addFriendBtn.setVisibility(View.INVISIBLE);
            friendsTxt.setText(R.string.your_profile_friendstxt);
            friendsTxt.setVisibility(View.VISIBLE);

        } else if (friends != null) {
            if (friends.contains(loggedInUserId)) {
                addFriendBtn.setVisibility(View.INVISIBLE);
                friendsTxt.setVisibility(View.VISIBLE);
            } else {
                checkFriendRequestStatus();
            }
        }
    }

    /**
     * function that determines the visibility of posts by privacy setting
     *
     * @param user the posts user
     */
    private void handlePrivacyOfPosts(User user) {
        // If user is public, friends or their own profile show posts
        if (!user.isPrivateProfile() || user.getUid().equals(loggedInUserId) || (user.getFriends() != null && user.getFriends().contains(loggedInUserId))) {
            initNewsfeed();
        } else {
            binding.userPostPrivateText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initialises the newsfeed views, the empty data observer and the swipe refresh layout.
     */
    private void initNewsfeed() {
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = binding.pagingRecycler;
        postsRecyclerView.setLayoutManager(layoutManager);

        postsAdapter = new PostsAdapter(postList, true, getActivity());
        postsRecyclerView.setAdapter(postsAdapter);

        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(postsRecyclerView,
                noPostsToShow);
        postsAdapter.registerAdapterDataObserver(emptyDataObserver);

        postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);

        Query originalQuery =
                postsRef.whereEqualTo(Constants.POST_USER_ID_FIELD, profileUserId).orderBy(Constants.TIMESTAMP_FIELD, Query.Direction.DESCENDING).limit(Constants.LIMIT);
        this.currentQuery = originalQuery;

        InitScrollingPosts.getPosts(originalQuery, true, postListViewModel, getActivity(),
                postsAdapter, postList);
        InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, this.currentQuery,
                postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                layoutManager);

        swipeRefreshLayout = binding.swipeContainer;
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(() -> updatePostFeed(currentQuery));
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Refreshes the newsfeed based on the new query
     *
     * @param query Firestore query
     */
    public void updatePostFeed(Query query) {
        postsAdapter.clear();
        postListViewModel.refreshData();
        this.currentQuery = query;
        InitScrollingPosts.getPosts(query, true, postListViewModel, getActivity(), postsAdapter,
                postList);
        InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, this.currentQuery,
                postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                layoutManager);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        if (postsAdapter != null) {
            postsAdapter.clear();
        }
    }

    /**
     * checkFriendRequestStatus determines if the logged in user and the profile that is being
     * shown have any existing friend requests between them, and the status of those requests.
     * The status will modify the visibility and content of UI elements:
     * - addFriendBtn
     * - friendsTxt
     */
    private void checkFriendRequestStatus() {
        CollectionReference friendRequestsRef =
                mFirestore.collection(Constants.FRIEND_REQUESTS_COLLECTION);

        Query query =
                friendRequestsRef.whereEqualTo(Constants.FRIEND_TO_ID, profileUserId).whereEqualTo(
                        Constants.FRIEND_FROM_ID, loggedInUserId).orderBy(Constants.TIMESTAMP_FIELD,
                        Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(documentSnapshots -> {
            addFriendBtn = binding.addFriendBtn;
            friendsTxt = binding.alreadyFriendsTxt;
            if (documentSnapshots.isEmpty()) {
                Log.d(TAG, "no pending request");
                addFriendBtn.setVisibility(View.VISIBLE);
                friendsTxt.setVisibility(View.INVISIBLE);

            } else {
                Log.d(TAG, "pending request");
                List<FriendRequest> friendRequests =
                        documentSnapshots.toObjects(FriendRequest.class);
                // should only be one
                FriendRequest fr = friendRequests.get(0);

                FriendRequestStatus status = FriendRequestStatus.valueOf(fr.getStatus());


                switch (status) {
                    case PENDING: {
                        addFriendBtn.setVisibility(View.INVISIBLE);
                        friendsTxt.setText(R.string.pending_request_txt);
                        friendsTxt.setVisibility(View.VISIBLE);
                        break;
                    }
                    case DENIED: {
                        addFriendBtn.setVisibility(View.VISIBLE);
                        friendsTxt.setVisibility(View.INVISIBLE);
                        break;
                    }
                    default: {
                        addFriendBtn.setVisibility(View.VISIBLE);
                        friendsTxt.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    /**
     * addFriend checks the user is not viewing their own profile before uploading to firebase.
     */
    private void addFriend() {
        if (loggedInUserId.equals(profileUserId)) {
            // the user is viewing their own profile.
            Log.d(TAG, "User can't add themselves");
            return;
        }

        uploadFriendToFirebase();
    }


    /**
     * uploadFriendToFirebase adds a FriendRequest to the FriendRequests Firebase collection,
     * adds the request id to the two users documents.
     */
    private void uploadFriendToFirebase() {
        CollectionReference friendRequestsRef =
                mFirestore.collection(Constants.FRIEND_REQUESTS_COLLECTION);
        DocumentReference userDocRef =
                mFirestore.collection(Constants.USERS_COLLECTION).document(loggedInUserId);
        DocumentReference profileUserDocRef =
                mFirestore.collection(Constants.USERS_COLLECTION).document(profileUserId);

        friendRequestsRef.add(new FriendRequest(loggedInUserId, profileUserId)).addOnSuccessListener(documentReference -> {
            if (postsAdapter != null) {
                postsAdapter.clear();
            }
            Log.d(TAG, "onSuccess: Add friend request");
            String requestId = documentReference.getId();

            // add id to object so it can update itself.
            DocumentReference specificRequestRef =
                    mFirestore.collection(Constants.FRIEND_REQUESTS_COLLECTION).document(requestId);
            specificRequestRef.update(Constants.FRIEND_REQUEST_ID, requestId);

            // add request to the 'TO' person so they can accept or deny it.
            profileUserDocRef.update(Constants.FRIEND_REQUESTS_COLLECTION,
                    FieldValue.arrayUnion(requestId));
            // add request to the 'FROM' person so we know there's a pending request to this
            // person already.
            userDocRef.update(Constants.FRIEND_REQUESTS_COLLECTION,
                    FieldValue.arrayUnion(requestId));


            // modify button/text
            addFriendBtn = binding.addFriendBtn;
            friendsTxt = binding.alreadyFriendsTxt;
            addFriendBtn.setVisibility(View.INVISIBLE);
            friendsTxt.setText(R.string.request_sent_txt);
            friendsTxt.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: Failed to add friend request"));
    }
}
