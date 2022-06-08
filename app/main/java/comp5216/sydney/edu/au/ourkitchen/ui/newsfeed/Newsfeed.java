package comp5216.sydney.edu.au.ourkitchen.ui.newsfeed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * A simple {@link Fragment} subclass for the newsfeed. Displays a friend's posts in a recycler
 * view.
 */
public class Newsfeed extends Fragment {
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    CollectionReference postsRef;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore mFirestore;
    private Query query;
    private FirebaseUser currentLoggedInUser;
    private LinearLayoutManager layoutManager;
    private ImageView filterBtn;
    private TextView filterTitle;
    private List<CheckBox> checkboxesList;

    private List<String> friends;
    private List<String> filterTags;
    private LinearLayout noPostsToShow;

    /**
     * Required empty public constructor
     */
    public Newsfeed() {
    }

    /**
     * Creates new instance of Newsfeed fragment.
     *
     * @return a new instance of fragment Newsfeed
     */
    public static Newsfeed newInstance() {
        Newsfeed fragment = new Newsfeed();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        mFirestore = FirebaseFirestore.getInstance();
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        currentLoggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.add_fab_btn);
        fab.setOnClickListener(view1 -> Navigation.findNavController(view1).navigate(R.id.action_newsfeed_to_createPost));

        initNewsfeed(view);
        setUpFilters(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        postsAdapter.clear();
    }

    /**
     * Helper function to set up the filter options based on the user's specified interests from
     * their profile. For each of their interests it will render a check box at the top of the
     * screen.
     *
     * @param view fragment view
     */
    private void setUpFilters(View view) {
        ConstraintLayout parentContainer = view.findViewById(R.id.container);
        Flow checkboxContainer = view.findViewById(R.id.checkbox_container);
        filterTitle = view.findViewById(R.id.filter_title);
        filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn.setOnClickListener(view1 -> clickFilterBtnAction());

        checkboxesList = new ArrayList<>();

        DocumentReference docRef =
                mFirestore.collection(Constants.USERS_COLLECTION).document(currentLoggedInUser.getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                CheckBox checkBox;
                if (user.getInterests().size() == 0) {
                    view.findViewById(R.id.filter_title).setVisibility(View.GONE);
                    filterBtn.setVisibility(View.GONE);
                    filterTitle.setVisibility(View.GONE);
                } else {
                    int[] referencesIds = new int[user.getInterests().size()];
                    int index = 0;
                    for (String interest : user.getInterests()) {
                        checkBox = new CheckBox(getActivity());
                        checkBox.setText(interest);
                        checkBox.setId(View.generateViewId());
                        checkboxesList.add(checkBox);
                        parentContainer.addView(checkBox);
                        referencesIds[index] = checkBox.getId();
                        index += 1;
                    }
                    checkboxContainer.setReferencedIds(referencesIds);

                    filterBtn.setVisibility(View.VISIBLE);
                    filterTitle.setVisibility(View.VISIBLE);
                }
                this.friends = user.getFriends();
                addPosts();
                removeLoadingBar(view);

            }
        });
    }

    /**
     * Function to remove the loading bar from view
     *
     * @param view The view to remove the loading bar from
     */
    private void removeLoadingBar(View view) {
        if (friends == null || friends.size() == 0) {
            EmptyDataObserver emptyDataObserver = new EmptyDataObserver(postsRecyclerView,
                    noPostsToShow);
            postsAdapter.registerAdapterDataObserver(emptyDataObserver);
        } else {
            postsRecyclerView.setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        view.findViewById(R.id.container).setVisibility(View.VISIBLE);
    }

    /**
     * Function to trigger filter changes and update post feed
     */
    private void clickFilterBtnAction() {
        List<String> filterTags = new ArrayList<>();
        for (CheckBox checkbox : checkboxesList) {
            if (checkbox.isChecked()) {
                filterTags.add(checkbox.getText().toString());
            }
        }
        this.filterTags = filterTags;
        updatePostFeed();
    }

    /**
     * Adds posts to the recycler view based on the current query.
     */
    private void addPosts() {
        if (friends != null && friends.size() > 0) {
            this.query =
                    postsRef.whereIn(Constants.POST_USER_ID_FIELD, friends).orderBy(Constants.TIMESTAMP_FIELD, Query.Direction.DESCENDING).limit(Constants.LIMIT);

            InitScrollingPosts.getPostsNewsfeed(this.query, true, postListViewModel,
                    getActivity(), postsAdapter, postList, friends);

            InitScrollingPosts.initRecyclerViewOnScrollListenerNewsfeed(isScrolling, this.query,
                    postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                    layoutManager, friends);
        }
    }

    /**
     * Initialises the newsfeed views, the empty data observer and the swipe refresh layout.
     *
     * @param view fragment view
     */
    private void initNewsfeed(View view) {
        noPostsToShow = view.findViewById(R.id.no_matching_results);
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = view.findViewById(R.id.paging_recycler);
        postsRecyclerView.setLayoutManager(layoutManager);

        postsAdapter = new PostsAdapter(postList, true, getActivity());
        postsRecyclerView.setAdapter(postsAdapter);

        postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);

        swipeRefreshLayout = view.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(this::updatePostFeed);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Refreshes the newsfeed based on the new query
     */
    public void updatePostFeed() {
        postsAdapter.clear();
        postListViewModel.refreshData();

        if (filterTags != null && filterTags.size() > 0) {
            if (friends != null && friends.size() > 0) {
                InitScrollingPosts.getPostsNewsfeedFilter(this.query, true, postListViewModel,
                        getActivity(), postsAdapter, postList, friends, filterTags);

                InitScrollingPosts.initRecyclerViewOnScrollListenerNewsfeedFilter(isScrolling,
                        this.query, postListViewModel, getActivity(), postsAdapter, postList,
                        postsRecyclerView, layoutManager, friends, filterTags);
            }
        } else {
            addPosts();
        }

        swipeRefreshLayout.setRefreshing(false);
    }
}
