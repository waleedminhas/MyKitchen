package comp5216.sydney.edu.au.ourkitchen.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * A simple {@link Fragment} subclass for Saved posts. Shows all the user's saved posts.
 */
public class Saved extends Fragment {
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    CollectionReference postsRef;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;
    private TextView mNoResults;

    private FirebaseAuth mFireAuth;
    private Query currentQuery;

    /**
     * Required empty public constructor
     */
    public Saved() {

    }

    /**
     * Creates a new instance of the Saved fragment.
     *
     * @return A new instance of fragment Saved.
     */
    public static Saved newInstance() {
        Saved fragment = new Saved();
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
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        mFireAuth = FirebaseAuth.getInstance();
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mNoResults = view.findViewById(R.id.no_matching_results);
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = view.findViewById(R.id.saved_recycler_view);
        postsRecyclerView.setLayoutManager(layoutManager);

        initScrollingSavedPosts(view);
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
     * Initialise the user's saved posts, query from Firestore and paginate to save bandwidth.
     *
     * @param view fragment view
     */
    private void initScrollingSavedPosts(View view) {
        if (mFireAuth.getCurrentUser() != null) {
            Query query = postsRef.whereArrayContains(Constants.POST_LIKED_BY_FIELD,
                    mFireAuth.getCurrentUser().getUid()).orderBy(Constants.TIMESTAMP_FIELD,
                    Query.Direction.DESCENDING).limit(Constants.LIMIT);
            this.currentQuery = query;
            this.currentQuery.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().size() > 0) {
                        mNoResults.setVisibility(View.GONE);
                        postsRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        mNoResults.setVisibility(View.VISIBLE);
                        postsRecyclerView.setVisibility(View.GONE);
                    }
                }
            });

            postsAdapter = new PostsAdapter(postList, true, getActivity());
            EmptyDataObserver emptyDataObserver = new EmptyDataObserver(postsRecyclerView,
                    mNoResults);
            postsAdapter.registerAdapterDataObserver(emptyDataObserver);

            postsRecyclerView.setAdapter(postsAdapter);

            postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);

            InitScrollingPosts.getPosts(query, true, postListViewModel, getActivity(),
                    postsAdapter, postList);
            InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, query,
                    postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                    layoutManager);
            swipeRefreshLayout = view.findViewById(R.id.saved_swipe_container);
            swipeRefreshLayout.setOnRefreshListener(this::fetchTimelineAsync);
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light, android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }
    }

    /**
     * Refreshes the list of posts when refresh called.
     */
    public void fetchTimelineAsync() {
        postsAdapter.clear();
        postListViewModel.refreshData();

        InitScrollingPosts.getPosts(this.currentQuery, true, postListViewModel, getActivity(),
                postsAdapter, postList);
        InitScrollingPosts.initRecyclerViewOnScrollListener(isScrolling, this.currentQuery,
                postListViewModel, getActivity(), postsAdapter, postList, postsRecyclerView,
                layoutManager);

        swipeRefreshLayout.setRefreshing(false);
    }
}
