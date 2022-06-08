package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.USERS_COLLECTION;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.images.ImageUtilies;
import comp5216.sydney.edu.au.ourkitchen.utils.images.RotateTransformation;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * Profile extends {@link Fragment}.
 * This is the users own profile with editable capabilities.
 */
public class Profile extends Fragment {
    private static final String TAG = "Profile";
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];

    // List adapter click listener for when a friend is clicked
    FriendsListAdapter.OnFriendClick listener = userId -> {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment);

            Bundle bundle = new Bundle();
            bundle.putString(Constants.USER_UID, userId);
            navController.navigate(R.id.showProfile, bundle);
        }
    };

    private TextView firstNameTxt;
    private TextView lastNameTxt;
    private TextView emailTxt;
    private Button editButton;
    private ImageView profileImage;
    private Uri imageUri;
    private Button viewFriendRequestsBtn;
    private File imageFile;
    private LinearLayout noMatchingResults;
    private TextView interestsTxt;
    private FriendsListAdapter friendsListAdapter;
    private List<User> friendsList;
    private FirebaseFirestore mFirestore;
    private StorageReference storageRef;
    private String userId;

    /**
     * image launcher for taking a new photo
     */
    ActivityResultLauncher<Uri> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    new ActivityResultCallback<Boolean>() {
                        @Override
                        public void onActivityResult(Boolean result) {
                            Bitmap bitmapImage;

                            try {
                                bitmapImage = decodeBitmap(imageUri);
                                int rotationInDegrees = ImageUtilies.getRotationInDegrees(imageFile);

                                if (getActivity() != null) {
                                    Glide.with(getActivity().getApplicationContext()).asBitmap().load(bitmapImage).transform(new RotateTransformation(rotationInDegrees)).into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource,
                                                                    @Nullable Transition<? super Bitmap> transition) {
                                            uploadToFireBase(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
    /**
     * launcher for selecting a photo from gallery
     */
    ActivityResultLauncher<String> mGetContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {
                            if (result == null) {
                                return;
                            }
                            Bitmap bitmapImage;
                            try {
                                bitmapImage = decodeBitmap(result);
                                int rotationInDegrees = ImageUtilies.getRotationInDegrees(result, getContext());
                                imageUri = result;
                                if (getActivity() != null) {
                                    Glide.with(getActivity().getApplicationContext()).asBitmap().load(bitmapImage).transform(new RotateTransformation(rotationInDegrees)).into(new CustomTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource,
                                                                    @Nullable Transition<? super Bitmap> transition) {
                                            uploadToFireBase(resource);
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

    private Context mContext;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private CollectionReference postsRef;
    private Query currentQuery;
    private NavController navController;

    /**
     * Required empty public constructor
     */
    public Profile() {
    }

    /**
     * Creates a new instance of this fragment
     *
     * @return the created instance of Profile
     */
    public static Profile newInstance() {
        Profile fragment = new Profile();
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
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        storageRef = FirebaseStorage.getInstance().getReference();
        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        }
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstNameTxt = view.findViewById(R.id.user_first_name);
        lastNameTxt = view.findViewById(R.id.user_last_name);
        noMatchingResults = view.findViewById(R.id.no_matching_results);
        emailTxt = view.findViewById(R.id.user_email);
        editButton = view.findViewById(R.id.edit_btn);
        profileImage = view.findViewById(R.id.user_image);
        viewFriendRequestsBtn = view.findViewById(R.id.pendingRequestsBtn);
        interestsTxt = view.findViewById(R.id.interests_list);

        if (friendsList == null) {
            friendsList = new ArrayList<>();
        }

        if (getActivity() != null) {
            navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        }

        setupButtonListeners(view);

        getPosts();
        setupUserInformation();

        initScrollingPosts(view);

        RecyclerView recyclerView = view.findViewById(R.id.friends_recycler_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        friendsListAdapter = new FriendsListAdapter(friendsList, listener);
        recyclerView.setAdapter(friendsListAdapter);
    }

    /**
     * set up click listeners for this fragment.
     *
     * @param view view
     */
    private void setupButtonListeners(View view) {
        // profile image listener
        profileImage.setOnClickListener(view12 -> {
            // profile image clicked
            // dialog
            showBottomSheet();
        });

        // create post button listener
        view.findViewById(R.id.create_post_btn).setOnClickListener(view1 -> goToCreatePost());

        // edit profile button listener
        editButton.setOnClickListener(view13 -> navController.navigate(R.id.action_profile_to_profileEdit));

        // view pending requests button listener
        viewFriendRequestsBtn.setOnClickListener(v -> navController.navigate(R.id.action_profile_to_pendingFriendRequests));
    }

    /**
     * Gets posts of this user from firebase.
     */
    private void getPosts() {
        mFirestore.collection(Constants.POSTS_COLLECTION).whereEqualTo(Constants.POST_USER_ID_FIELD, userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("Profile Posts", document.getId() + " => " + document.getData());
                }
            } else {
                Log.d("Profile Posts", "onComplete: Error");
            }
        });
    }

    /**
     * load the users information into the UI from firebase
     */
    private void setupUserInformation() {
        DocumentReference documentReference =
                mFirestore.collection(USERS_COLLECTION).document(userId);
        documentReference.addSnapshotListener((value, error) -> {
            if (value != null) {
                User user = value.toObject(User.class);
                if (user != null) {
                    Log.d(TAG, user.toString());

                    firstNameTxt.setText(user.getFirstName());
                    lastNameTxt.setText(user.getLastName());
                    emailTxt.setText(user.getEmail());

                    String profilePicURI = user.getProfilePhoto();
                    if (mContext != null && profilePicURI != null && !profilePicURI.equals(("")) && getActivity() != null) {
                        Glide.with(getActivity().getApplicationContext()).load(profilePicURI).into(profileImage);
                    }

                    String userInterests = user.getInterestsAsString();
                    if (userInterests.length() != 0) {
                        interestsTxt.setText(userInterests);
                    }

                    List<String> friendsIDlist = user.getFriends();
                    if (friendsIDlist != null) {
                        if (!friendsIDlist.isEmpty()) {
                            for (String friendId : friendsIDlist) {
                                DocumentReference friendRef =
                                        mFirestore.collection(USERS_COLLECTION).document(friendId);
                                friendRef.addSnapshotListener((value1, error1) -> {
                                    if (value1 != null) {
                                        User friend = value1.toObject(User.class);
                                        if (friend != null) {
                                            boolean exists =
                                                    friendsList.stream().anyMatch(f -> (f != null && f.getUid().equals(friend.getUid())));
                                            if (!exists) {
                                                friendsList.add(friend);
                                                friendsListAdapter.notifyDataSetChanged();
                                            }
                                        }

                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Navigates to the create post fragment
     */
    private void goToCreatePost() {
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment);
            navController.navigate(R.id.createPost);
        }
    }

    /**
     * Initializes posts
     *
     * @param view the view for the posts
     */
    private void initScrollingPosts(View view) {
        // Set up the list of posts
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = view.findViewById(R.id.paging_recycler);
        postsRecyclerView.setLayoutManager(layoutManager);

        postList.clear();
        postsAdapter = new PostsAdapter(postList, false, getActivity());
        postsRecyclerView.setAdapter(postsAdapter);
        postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);

        Query query =
                postsRef.whereEqualTo(Constants.POST_USER_ID_FIELD, userId).orderBy(Constants.TIMESTAMP_FIELD, Query.Direction.DESCENDING).limit(Constants.LIMIT);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().size() == 0) {
                    noMatchingResults.setVisibility(View.VISIBLE);
                } else {
                    noMatchingResults.setVisibility(View.GONE);
                }
            }
        });
        this.currentQuery = query;

        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(postsRecyclerView,
                noMatchingResults);
        postsAdapter.registerAdapterDataObserver(emptyDataObserver);

        InitScrollingPosts.getPosts(query, true, postListViewModel, getActivity(), postsAdapter,
                postList);
        InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, query, postListViewModel
                , getActivity(), postsAdapter, postList, postsRecyclerView, layoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.profile_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this::fetchTimelineAsync);

        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Uploads an image to firebase for the users profile photo, gets the reference and saves that
     * reference in the user.
     *
     * @param image bitmap for profile photo
     */
    private void uploadToFireBase(Bitmap image) {
        StorageReference imageRef = storageRef.child("images/" + userId + "-" + imageUri + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.WEBP, 75, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // successful upload, get the image uri.
            storageRef.child("images/" + userId + "-" + imageUri + ".jpg").getDownloadUrl().addOnSuccessListener(uri -> {
                // image uri
                DocumentReference documentReference =
                        mFirestore.collection(USERS_COLLECTION).document(userId);
                documentReference.update("profilePhoto", uri.toString());

            }).addOnFailureListener(e -> Log.d(TAG, "onFailure: Failed to get download uri"));
        }).addOnFailureListener(e -> {
            // failed to upload image
            Log.d(TAG, "onFailure: image upload failure");
        });
    }

    /**
     * Decodes an image
     *
     * @param selectedImage uri of image
     * @return Bitmap from the uri
     * @throws FileNotFoundException file not found exception
     */
    public Bitmap decodeBitmap(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        if (getActivity() != null) {
            return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, options);
        }
        return null;
    }

    /**
     * Launch the camera intent and get the image file on return.
     */
    private void dispatchTakePictureIntent() {
        if (getActivity() != null) {
            imageFile = new File(getActivity().getExternalMediaDirs()[0],
                    "userprofilePicFromCamera");
            imageUri = FileProvider.getUriForFile(getActivity(),
                    getActivity().getPackageName() + ".fileprovider", imageFile);
            imageLauncher.launch(imageUri);
        }
    }

    /**
     * Fetches the posts
     */
    public void fetchTimelineAsync() {
        postsAdapter.clear();
        postListViewModel.refreshData();
        InitScrollingPosts.getPosts(currentQuery, true, postListViewModel, getActivity(),
                postsAdapter, postList);
        InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, currentQuery,
                postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                layoutManager);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Launches the bottom sheet to select take a photo, select a photo, or cancel
     */
    public void showBottomSheet() {
        if (getActivity() != null) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
            bottomSheetDialog.setContentView(R.layout.profile_select_photo_bottom_sheet);
            Button selectPhoto = bottomSheetDialog.findViewById(R.id.select_profile_photo);
            Button takePhoto = bottomSheetDialog.findViewById(R.id.take_profile_photo);
            Button cancelSheet = bottomSheetDialog.findViewById(R.id.select_profile_cancel);

            if (selectPhoto != null) {
                selectPhoto.setOnClickListener(v -> {
                    mGetContent.launch("image/*");
                    bottomSheetDialog.dismiss();
                });
            }
            if (takePhoto != null) {
                takePhoto.setOnClickListener(v -> {
                    dispatchTakePictureIntent();
                    bottomSheetDialog.dismiss();
                });
            }

            if (cancelSheet != null) {
                cancelSheet.setOnClickListener(v -> bottomSheetDialog.dismiss());
            }

            bottomSheetDialog.show();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        postsAdapter.clear();
    }
}
