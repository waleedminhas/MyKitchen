package comp5216.sydney.edu.au.ourkitchen.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * A simple {@link Fragment} subclass for Search fragment. Enables the users to
 * search for posts via tags.
 */
public class Search extends Fragment {
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    Button mInternalBtn;
    EditText mSearchText;
    TextView mNoMatchingResults;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private LinearLayoutManager layoutManager;

    private FirebaseFirestore mFirestore;
    private FirebaseUser currentUser;
    private String mParamSearch;
    private boolean isSavedSearch;

    /**
     * Required empty public constructor
     */
    public Search() {
    }

    /**
     * Creates new instance of the search fragment.
     *
     * @return A new instance of fragment Search.
     */
    public static Search newInstance() {
        Search fragment = new Search();
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
        item = menu.findItem(R.id.find_friends_icon);
        if (item != null) {
            item.setVisible(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mParamSearch = SearchArgs.fromBundle(getArguments()).getSearchType();
        isSavedSearch = mParamSearch.equals(Constants.SAVED_SEARCH);
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpViews(view);
        setUpSearchBtns(view);
        setUpListOfPosts(view);

        postsRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Helper function to find all the views in the fragment and initialise
     *
     * @param view fragment view
     */
    private void setUpViews(View view) {
        mSearchText = view.findViewById(R.id.edittext);
        mNoMatchingResults = view.findViewById(R.id.no_matching_results);

        if (isSavedSearch) {
            mNoMatchingResults.setText(R.string.you_have_no_matching_saved_posts_txt);
        }
    }

    /**
     * Helper function to init the post recycler view with Firestore query
     *
     * @param view fragment view
     */
    private void setUpListOfPosts(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = view.findViewById(R.id.paging_recycler);
        postsRecyclerView.setLayoutManager(layoutManager);

        postsAdapter = new PostsAdapter(postList, true, getActivity());
        postsRecyclerView.setAdapter(postsAdapter);

        postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);
    }

    /**
     * Helper function to set up the search button click listeners
     *
     * @param view fragment view
     */
    private void setUpSearchBtns(View view) {
        mInternalBtn = view.findViewById(R.id.search_internal_btn);
        mInternalBtn.setOnClickListener(view1 -> searchOurKitchen());
    }

    /**
     * Execute when search button is clicked
     */
    private void searchOurKitchen() {
        // Hide keyboard when comment button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        postsRecyclerView.setVisibility(View.VISIBLE);

        // Convert string to Firestore query
        String stringQuery = mSearchText.getText().toString();
        if (!stringQuery.equals("")) {
            CollectionReference postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);

            Query query = postsRef.whereArrayContainsAny(Constants.POST_TAG_FIELD,
                    Arrays.asList(stringQuery.split(" "))).orderBy(Constants.TIMESTAMP_FIELD,
                    Query.Direction.DESCENDING).limit(Constants.LIMIT);
            executeQuery(query);
        } else {
            Utils.showToast(getActivity(), getString(R.string.enter_a_search_term));
        }
    }

    /**
     * Helper function to execute the user query and update the search results
     *
     * @param query search query
     */
    private void executeQuery(Query query) {
        postsAdapter.clear();
        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(postsRecyclerView,
                mNoMatchingResults);
        postsAdapter.registerAdapterDataObserver(emptyDataObserver);

        if (mParamSearch.equals(Constants.SAVED_SEARCH)) {
            InitScrollingPosts.getPosts(query, true, postListViewModel, getActivity(),
                    postsAdapter, postList, currentUser.getUid());
            InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, query,
                    postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                    layoutManager);
        } else {
            mFirestore.collection(Constants.USERS_COLLECTION).document(currentUser.getUid()).addSnapshotListener((value, error) -> {
                if (value != null) {
                    User user = value.toObject(User.class);
                    if (user != null) {
                        InitScrollingPosts.getPostsNewsfeedSearch(query, true, postListViewModel,
                                getActivity(), postsAdapter, postList, user.getFriends());
                        InitScrollingPosts.initRecyclerViewOnScrollListenerNewsfeedSearch(isScrolling, query, postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView, layoutManager, user.getFriends());
                    }
                }
            });
        }
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
}
