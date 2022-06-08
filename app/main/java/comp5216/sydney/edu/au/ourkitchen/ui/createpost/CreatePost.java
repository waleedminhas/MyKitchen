package comp5216.sydney.edu.au.ourkitchen.ui.createpost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.utils.UploadWorker;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;
import comp5216.sydney.edu.au.ourkitchen.utils.images.ImageUtilies;
import comp5216.sydney.edu.au.ourkitchen.utils.images.RotateTransformation;

/**
 * A simple {@link Fragment} subclass for CreatePost. Enables the user to create a post
 * specifying their review and uploading a photo and link to recipe.
 */
public class CreatePost extends Fragment {
    public static final String PHOTO_URI = "PHOTO_URI";
    public static final String POST_USER_ID = "userID";
    public static final String POST_RATING = "POST_RATING";
    public static final String POST_LINK = "POST_LINK";
    public static final String POST_MODIFICATION = "POST_MODIFICATION";
    public static final String POST_COMMENT = "POST_COMMENT";
    public static final String POST_TAGS = "POST_TAGS";
    private final Constraints constraintRequireWifi =
            new Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build();
    private final Constraints noConstraints = new Constraints.Builder().build();
    ActivityResultLauncher<String> mGetContent;
    ActivityResultLauncher<Uri> mTakePhoto;
    private EditText linkInput, modificationInput, commentInput, hashTagInput;
    private RatingBar ratingInput;
    private ImageView imagePreview;
    private Uri uriOfPhoto;
    private Bitmap bitmapImage;
    private File imageFile;
    private FirebaseAuth firebaseAuth;
    private AlertDialog loader;
    private boolean requireWifi;

    /**
     * Required empty public constructor for fragment
     */
    public CreatePost() {
    }

    /**
     * Helper function to decode bitmap from Uri
     *
     * @param activity      context
     * @param selectedImage image uri
     * @return bitmap
     * @throws FileNotFoundException when file is not found
     */
    public static Bitmap decodeBitmap(Context activity, Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, options);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreatePost.
     */
    public static CreatePost newInstance() {
        CreatePost fragment = new CreatePost();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Notify fragment there are menu options
     *
     * @param savedInstanceState saved instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        return inflater.inflate(R.layout.fragment_createpost, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Find views
        commentInput = view.findViewById(R.id.commentInput);
        linkInput = view.findViewById(R.id.linkInput);
        modificationInput = view.findViewById(R.id.modificationInput);
        ratingInput = view.findViewById(R.id.ratingBar);
        hashTagInput = view.findViewById(R.id.hashTagInput);
        imagePreview = view.findViewById(R.id.imagePreview);

        // Set up buttons
        Button selectPicsBtn = view.findViewById(R.id.selectPicsBtn);
        selectPicsBtn.setOnClickListener(view12 -> mGetContent.launch("image/*"));
        setUpTakePicBtn(view);
        implementPostBtn(view);
        setUpBackBtn(view);
        initGetMediaActivities();

        setHasOptionsMenu(true);

        if (getActivity() != null) {
            Utils.setUpCloseKeyboardOnOutsideClick(view.findViewById(R.id.parent), getActivity());
        }

        // Get the user's preference for WiFi or cellular
        if (getContext() != null) {
            requireWifi =
                    !PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.cellular_pref_key), true);
        }
    }

    /**
     * Helper function to initialise the select a photo and take photo activiites.
     */
    private void initGetMediaActivities() {
        if (getActivity() != null && getContext() != null) {
            mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result == null) {
                            return;
                        }

                        // Handle the returned Uri
                        try {
                            bitmapImage = decodeBitmap(getActivity(), result);
                            imagePreview.setImageBitmap(bitmapImage);
                            uriOfPhoto = result;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

            mTakePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    result -> {
                        try {
                            bitmapImage = decodeBitmap(getActivity(), uriOfPhoto);

                            // handle image rotation and set image
                            int rotationInDegrees = ImageUtilies.getRotationInDegrees(imageFile);
                            Glide.with(getContext()).asBitmap().load(bitmapImage).transform(new RotateTransformation(rotationInDegrees)).into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource,
                                                            @Nullable Transition<? super Bitmap> transition) {
                                    bitmapImage = resource;
                                    imagePreview.setImageBitmap(bitmapImage);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
        }
    }

    /**
     * Helper function to initialise the take picture button.
     *
     * @param view fragment view
     */
    private void setUpTakePicBtn(View view) {
        Button takePicBtn = view.findViewById(R.id.takePicture);
        takePicBtn.setOnClickListener(view13 -> {
            if (getActivity() != null && getActivity().getExternalMediaDirs() != null) {
                imageFile = new File(getActivity().getExternalMediaDirs()[0], "newPicFromCamera");
                uriOfPhoto = FileProvider.getUriForFile(getActivity(),
                        getActivity().getPackageName() + ".fileprovider", imageFile);
                mTakePhoto.launch(uriOfPhoto);
            }
        });
    }

    /**
     * Helper function to initialise the back button.
     *
     * @param view fragment view
     */
    private void setUpBackBtn(View view) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle(getString(R.string.cancel_post)).setMessage(getString(R.string.create_post_leave_confirmation_message)).setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> Navigation.findNavController(view).navigateUp()).setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                });
                builder.create().show();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                callback);
    }

    /**
     * Helper function to initialise the take post button.
     *
     * @param view fragment view
     */
    private void implementPostBtn(View view) {
        Context activity = getActivity();
        if (activity != null) {
            Button postBtn = view.findViewById(R.id.postBtn);
            postBtn.setOnClickListener(view1 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(getString(R.string.share_post)).setMessage(getString(R.string.ready_to_post)).setPositiveButton(
                        getString(R.string.yes), (dialogInterface, i) -> {
                            createLoaderDialog();

                            // Extract user input
                            String comment = commentInput.getText().toString();
                            String link = linkInput.getText().toString();
                            String modification = modificationInput.getText().toString();
                            float rating = ratingInput.getRating();
                            List<String> hashtags = extractHashTags(hashTagInput.getText().toString());
                            String userID = firebaseAuth.getCurrentUser() != null ?
                                    firebaseAuth.getCurrentUser().getUid() : "";

                            Post post = new Post(userID, null, rating, link, modification, comment, null,
                                    new Timestamp(new Date()), hashtags);
                            if (!requireWifi) {
                                if (!isConnected()) {
                                    Utils.showToast(activity, activity.getResources().getString(R.string.post_offline_message));
                                }
                                enqueueToWorkManager(post, noConstraints);
                            } else {
                                if (!isConnectedToWifi()) {
                                    Utils.showToast(activity, getString(R.string.post_will_upload_when_wifi));
                                }
                                enqueueToWorkManager(post, constraintRequireWifi);
                            }
                        }).setNegativeButton(getString(R.string.no), (dialogInterface, i) -> { /* Do nothing */ });
                builder.create().show();
            });
        }
    }

    /**
     * Helper function to determine whether connected to a network
     *
     * @return isConnectedToNetwork
     */
    private boolean isConnected() {
        boolean isConnected = false;
        if (getActivity() != null) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            for (Network network : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                isConnected |= networkInfo.isConnected();
            }
        }
        return isConnected;
    }

    /**
     * Helper function to determine whether connected to wifi
     *
     * @return isConnectedToWifi
     */
    private boolean isConnectedToWifi() {
        boolean isWifiConn = false;
        if (getActivity() != null) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            for (Network network : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn |= networkInfo.isConnected();
                }
            }
        }
        return isWifiConn;
    }

    /**
     * Helper function to enqueue a post to the work manager.
     *
     * @param post        post
     * @param constraints constraints, whether the post can be posted using cellular or WiFi
     */
    private void enqueueToWorkManager(Post post, Constraints constraints) {
        Context ctx = getActivity();
        String[] tagArray = new String[post.getTags().size()];
        tagArray = post.getTags().toArray(tagArray);
        WorkRequest myUploadWork =
                new OneTimeWorkRequest.Builder(UploadWorker.class).setConstraints(constraints).setInputData(new Data.Builder().putString(PHOTO_URI, uriOfPhoto != null ? uriOfPhoto.toString() : null).putString(POST_USER_ID, post.getUserID()).putFloat(POST_RATING, post.getRating()).putString(POST_LINK, post.getRecipeUrl()).putString(POST_MODIFICATION, post.getModification()).putString(POST_COMMENT, post.getComment()).putStringArray(POST_TAGS, tagArray).build()).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS).build();

        if (ctx != null) {
            WorkManager.getInstance(ctx).enqueue(myUploadWork);
        }
        exitCreatePost();
    }

    /**
     * Helper function to extract the hashtags from the user input
     *
     * @param string hashtags as a single string
     * @return list of hash tags in lowercase
     */
    private List<String> extractHashTags(String string) {
        Matcher matcher = Pattern.compile("(#[^@\\s]*)").matcher(string);

        List<String> tags = new ArrayList<>();
        while (matcher.find()) {
            tags.add(matcher.group().substring(1).toLowerCase());
        }

        return tags;
    }

    /**
     * Helper function to leave the create post fragment.
     */
    private void exitCreatePost() {
        loader.dismiss();
        if (getActivity() != null) {
            NavController navController = Navigation.findNavController(getActivity(),
                    R.id.nav_host_fragment);
            navController.popBackStack();
        }
    }

    /**
     * Helper function creates the loader alert dialog.
     */
    private void createLoaderDialog() {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setTitle(getString(R.string.loading)).setMessage(getString(R.string.posting_now));
            loader = builder.create();
            loader.show();
        }
    }
}
