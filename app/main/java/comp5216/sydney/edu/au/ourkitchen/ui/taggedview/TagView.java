package comp5216.sydney.edu.au.ourkitchen.ui.taggedview;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.POST_TAG_FIELD;
import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.TIMESTAMP_FIELD;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.InitScrollingPosts;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.postrecycler.PostsAdapter;

/**
 * A simple {@link Fragment} subclass for TagView fragment. This will display all the related
 * posts matching the current tag.
 */
public class TagView extends Fragment {
    private final List<Post> postList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private String tag;
    private RecyclerView postsRecyclerView;
    private PostsAdapter postsAdapter;
    private PostListViewModel postListViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager layoutManager;

    private CollectionReference postsRef, usersRef;
    private Query currentQuery;
    private String currentUserId;
    private User user;

    /**
     * Required empty public constructor
     */
    public TagView() {

    }

    /**
     * Creates a new instance of TagView fragment.
     *
     * @return A new instance of fragment tag view.
     */
    public static TagView newInstance() {
        TagView fragment = new TagView();
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
        if (getArguments() != null) {
            tag = getArguments().getString(Constants.TAG_VIEW_TAG_KEY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_view, container, false);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        postsRef = mFirestore.collection(Constants.POSTS_COLLECTION);
        usersRef = mFirestore.collection(Constants.USERS_COLLECTION);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView mTagTitle = view.findViewById(R.id.tagTitle);
        mTagTitle.setText(String.format("#%s", tag));

        initScrollingTaggedPosts(view);
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
     * Initialises the recycler view of posts, the empty data observer and the swipe refresh
     * layout.
     *
     * @param view fragment view
     */
    private void initScrollingTaggedPosts(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView = view.findViewById(R.id.tag_recycler_view);
        postsRecyclerView.setLayoutManager(layoutManager);

        postsAdapter = new PostsAdapter(postList, true, getActivity());
        postsRecyclerView.setAdapter(postsAdapter);
        postListViewModel = new ViewModelProvider(this).get(PostListViewModel.class);

        Query query =
                postsRef.whereArrayContains(POST_TAG_FIELD, this.tag).orderBy(TIMESTAMP_FIELD,
                        Query.Direction.DESCENDING).limit(Constants.LIMIT);

        this.currentQuery = query;

        usersRef.document(currentUserId).addSnapshotListener((value, error) -> {
            if (value != null) {
                User user = value.toObject(User.class);
                if (user != null) {
                    this.user = user;
                    InitScrollingPosts.getPostsNewsfeedSearch(query, true, postListViewModel,
                            getActivity(), postsAdapter, postList, user.getFriends());
                    InitScrollingPosts.initRecyclerViewOnScrollListenerNewsfeedSearch(isScrolling
                            , query, postListViewModel, getActivity(), postsAdapter, postList,
                            postsRecyclerView, layoutManager, user.getFriends());
                }
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this::fetchTimelineAsync);

        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Refreshes the list of posts on swipe refresh.
     */
    public void fetchTimelineAsync() {
        postsAdapter.clear();
        postListViewModel.refreshData();
        InitScrollingPosts.getPostsNewsfeedSearch(currentQuery, true, postListViewModel,
                getActivity(), postsAdapter, postList, user.getFriends());
        InitScrollingPosts.initRecyclerViewOnScrollListenerNewsfeedSearch(isScrolling,
                currentQuery, postListViewModel, getActivity(), postsAdapter, postList,
                postsRecyclerView, layoutManager, user.getFriends());
        swipeRefreshLayout.setRefreshing(false);
    }
}
