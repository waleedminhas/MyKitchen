package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;

import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * Helper class to initialise the scrolling posts for different fragments.
 */
public class InitScrollingPosts {
    /**
     * Helper method to get post from the newsfeed, will observe changes in the Firestore database
     *
     * @param query             Firestore query
     * @param isNewQuery        whether the query is new or not
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          list of posts
     */
    public static void getPosts(Query query, boolean isNewQuery,
                                PostListViewModel postListViewModel, FragmentActivity activity,
                                PostsAdapter postsAdapter, List<Post> postList) {
        PostListLiveData postListLiveData = postListViewModel.getPostListLiveData(query,
                isNewQuery);
        if (postListLiveData != null && activity != null) {
            postListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Post addedPost = operation.post;
                        addPost(addedPost, postList);
                        break;

                    case R.string.modified:
                        Post modifiedPost = operation.post;
                        modifyPost(modifiedPost, postList);
                        break;

                    case R.string.removed:
                        Post removedPost = operation.post;
                        removePost(removedPost, postList);
                }
                postsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Helper method to get post from the newsfeed, will observe changes in the Firestore database
     *
     * @param query             Firestore query
     * @param isNewQuery        whether the query is new or not
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          list of posts
     */
    public static void getPostsNewsfeed(Query query, boolean isNewQuery,
                                        PostListViewModel postListViewModel,
                                        FragmentActivity activity, PostsAdapter postsAdapter,
                                        List<Post> postList, List<String> friends) {
        PostListLiveData postListLiveData = postListViewModel.getPostListLiveData(query,
                isNewQuery);
        if (postListLiveData != null && activity != null) {
            postListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Post addedPost = operation.post;
                        addPostNewsfeed(addedPost, postList, friends);
                        break;

                    case R.string.modified:
                        Post modifiedPost = operation.post;
                        modifyPost(modifiedPost, postList);
                        break;

                    case R.string.removed:
                        Post removedPost = operation.post;
                        removePost(removedPost, postList);
                }
                postsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Helper method to get post from the newsfeed, will observe changes in the Firestore database
     *
     * @param query             Firestore query
     * @param isNewQuery        whether the query is new or not
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          list of posts
     * @param filterTags        list of tags to filter by
     */
    public static void getPostsNewsfeedFilter(Query query, boolean isNewQuery,
                                              PostListViewModel postListViewModel,
                                              FragmentActivity activity, PostsAdapter postsAdapter,
                                              List<Post> postList, List<String> friends,
                                              List<String> filterTags) {
        PostListLiveData postListLiveData = postListViewModel.getPostListLiveData(query,
                isNewQuery);
        if (postListLiveData != null && activity != null) {
            postListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Post addedPost = operation.post;
                        addPostNewsfeedFilter(addedPost, postList, friends, filterTags);
                        break;

                    case R.string.modified:
                        Post modifiedPost = operation.post;
                        modifyPost(modifiedPost, postList);
                        break;

                    case R.string.removed:
                        Post removedPost = operation.post;
                        removePost(removedPost, postList);
                }
                postsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Helper method to get post for the user's saved posts
     *
     * @param query             Firestore query
     * @param isNewQuery        whether the query is new or not
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          list of posts
     * @param currentUserId     current logged in user UID
     */
    public static void getPosts(Query query, boolean isNewQuery,
                                PostListViewModel postListViewModel, FragmentActivity activity,
                                PostsAdapter postsAdapter, List<Post> postList,
                                String currentUserId) {
        PostListLiveData postListLiveData = postListViewModel.getPostListLiveData(query,
                isNewQuery);
        if (postListLiveData != null && activity != null) {
            postListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Post addedPost = operation.post;
                        addPost(addedPost, postList, currentUserId);
                        break;

                    case R.string.modified:
                        Post modifiedPost = operation.post;
                        modifyPost(modifiedPost, postList);
                        break;

                    case R.string.removed:
                        Post removedPost = operation.post;
                        removePost(removedPost, postList);
                }
                postsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Get posts for the newsfeed search
     *
     * @param query             Firestore query
     * @param isNewQuery        whether the query is new or not
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          list of posts
     * @param friends           list of current user's friends
     */
    public static void getPostsNewsfeedSearch(Query query, boolean isNewQuery,
                                              PostListViewModel postListViewModel,
                                              FragmentActivity activity,
                                              PostsAdapter postsAdapter, List<Post> postList,
                                              List<String> friends) {
        PostListLiveData postListLiveData = postListViewModel.getPostListLiveData(query,
                isNewQuery);
        if (postListLiveData != null && activity != null) {
            postListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Post addedPost = operation.post;
                        addPostNewsfeedSearch(addedPost, postList, friends, postsAdapter);
                        break;

                    case R.string.modified:
                        Post modifiedPost = operation.post;
                        modifyPost(modifiedPost, postList);
                        break;

                    case R.string.removed:
                        Post removedPost = operation.post;
                        removePost(removedPost, postList);
                }
                postsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Add post to adapter for newsfeed search
     *
     * @param addedPost  post to add
     * @param postList   current list of posts
     * @param friends    current logged in user's friends
     * @param filterTags tags to filter newsfeed by
     */
    private static void addPostNewsfeedFilter(Post addedPost, List<Post> postList,
                                              List<String> friends, List<String> filterTags) {
        if (!postList.contains(addedPost) && friends.contains(addedPost.getUserID()) && addedPost.getTags() != null && addedPost.getTags().stream().anyMatch(filterTags::contains)) {
            postList.add(addedPost);
        }
    }

    /**
     * Add post to adapter for newsfeed search
     *
     * @param addedPost    post to add
     * @param postList     current list of posts
     * @param friends      current logged in user's friends
     * @param postsAdapter post recycler adapter
     */
    private static void addPostNewsfeedSearch(Post addedPost, List<Post> postList,
                                              List<String> friends, PostsAdapter postsAdapter) {
        if (postList.contains(addedPost)) {
            return;
        }

        if (friends.contains(addedPost.getUserID())) {
            postList.add(addedPost);
        } else {
            FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION).document(addedPost.getUserID()).addSnapshotListener((value, error) -> {
                if (value != null) {
                    User user = value.toObject(User.class);
                    if (user != null) {
                        if (!user.isPrivateProfile()) {
                            postList.add(addedPost);
                            postsAdapter.notifyDataSetChanged();
                        }
                    }
                }

            });
        }
    }

    /**
     * Updates the post list when a post is added so the recycler view will update
     *
     * @param addedPost post to add
     * @param postList  current list of posts
     */
    private static void addPost(Post addedPost, List<Post> postList) {
        if (!postList.contains(addedPost)) {
            postList.add(addedPost);
        }
    }

    /**
     * Updates the post list when a post is added so the recycler view will update
     *
     * @param addedPost post to add
     * @param postList  current list of posts
     */
    private static void addPost(Post addedPost, List<Post> postList, String currentUserId) {
        if (addedPost.getLikedBy().contains(currentUserId) && !postList.contains(addedPost)) {
            postList.add(addedPost);
        }
    }

    /**
     * Updates the post list when a post is added so the recycler view will update
     *
     * @param addedPost post to add
     * @param postList  current post list
     * @param friends   list of user's friends
     */
    private static void addPostNewsfeed(Post addedPost, List<Post> postList, List<String> friends) {
        if (friends != null && friends.contains(addedPost.getUserID()) && !postList.contains(addedPost)) {
            postList.add(addedPost);
        }
    }

    /**
     * Updates the post list when a post is modified so the recycler view will update
     *
     * @param modifiedPost a modified post
     */
    private static void modifyPost(Post modifiedPost, List<Post> postList) {
        for (int i = 0; i < postList.size(); i++) {
            Post currentPost = postList.get(i);
            if (modifiedPost != null && currentPost != null && currentPost.getId().equals(modifiedPost.getId())) {
                postList.remove(currentPost);
                postList.add(i, modifiedPost);

            }
        }
    }

    /**
     * Removes a post from the post list so it is reflected in the recycler view.
     *
     * @param removedPost post to remove
     * @param postList    current list of posts
     */
    private static void removePost(Post removedPost, List<Post> postList) {
        for (int i = 0; i < postList.size(); i++) {
            Post currentPost = postList.get(i);
            if (currentPost.getId().equals(removedPost.getId())) {
                postList.remove(currentPost);
                break;
            }
        }
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling       whether user is scrolling
     * @param currentQuery      current query
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          post list
     * @param postsRecyclerView recycler view for posts
     * @param layoutManager     layout manager
     */
    public static void initRecyclerViewOnScrollListener(boolean[] isScrolling, Query currentQuery
            , PostListViewModel postListViewModel, FragmentActivity activity,
                                                        PostsAdapter postsAdapter,
                                                        List<Post> postList,
                                                        RecyclerView postsRecyclerView,
                                                        LinearLayoutManager layoutManager) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisiblePostPosition = layoutManager.findFirstVisibleItemPosition();
                    int visiblePostCount = layoutManager.getChildCount();
                    int totalPostCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisiblePostPosition + visiblePostCount == totalPostCount)) {
                        isScrolling[0] = false;
                        getPosts(currentQuery, false, postListViewModel, activity, postsAdapter,
                                postList);
                    }
                }
            }
        };
        postsRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling       whether user is scrolling
     * @param currentQuery      current query
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          post list
     * @param postsRecyclerView recycler view for posts
     * @param layoutManager     layout manager
     * @param friends           list of current user's friends
     */
    public static void initRecyclerViewOnScrollListenerNewsfeed(boolean[] isScrolling,
                                                                Query currentQuery,
                                                                PostListViewModel postListViewModel, FragmentActivity activity, PostsAdapter postsAdapter, List<Post> postList, RecyclerView postsRecyclerView, LinearLayoutManager layoutManager, List<String> friends) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisiblePostPosition = layoutManager.findFirstVisibleItemPosition();
                    int visiblePostCount = layoutManager.getChildCount();
                    int totalPostCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisiblePostPosition + visiblePostCount == totalPostCount)) {
                        isScrolling[0] = false;
                        getPostsNewsfeed(currentQuery, false, postListViewModel, activity,
                                postsAdapter, postList, friends);
                    }
                }
            }
        };
        postsRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling       whether user is scrolling
     * @param currentQuery      current query
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          post list
     * @param postsRecyclerView recycler view for posts
     * @param layoutManager     layout manager
     * @param friends           list of current user's friends
     * @param filterTags        list of filter queries
     */
    public static void initRecyclerViewOnScrollListenerNewsfeedFilter(boolean[] isScrolling,
                                                                      Query currentQuery,
                                                                      PostListViewModel postListViewModel, FragmentActivity activity, PostsAdapter postsAdapter, List<Post> postList, RecyclerView postsRecyclerView, LinearLayoutManager layoutManager, List<String> friends, List<String> filterTags) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisiblePostPosition = layoutManager.findFirstVisibleItemPosition();
                    int visiblePostCount = layoutManager.getChildCount();
                    int totalPostCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisiblePostPosition + visiblePostCount == totalPostCount)) {
                        isScrolling[0] = false;
                        getPostsNewsfeedFilter(currentQuery, false, postListViewModel, activity,
                                postsAdapter, postList, friends, filterTags);
                    }
                }
            }
        };
        postsRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling for the
     * newsfeed search
     *
     * @param isScrolling       whether user is scrolling
     * @param currentQuery      current query
     * @param postListViewModel post list view model
     * @param activity          activity context
     * @param postsAdapter      post adapter
     * @param postList          post list
     * @param postsRecyclerView recycler view for posts
     * @param layoutManager     layout manager
     * @param friends           list of current user's friends
     */
    public static void initRecyclerViewOnScrollListenerNewsfeedSearch(boolean[] isScrolling,
                                                                      Query currentQuery,
                                                                      PostListViewModel postListViewModel, FragmentActivity activity, PostsAdapter postsAdapter, List<Post> postList, RecyclerView postsRecyclerView, LinearLayoutManager layoutManager, List<String> friends) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisiblePostPosition = layoutManager.findFirstVisibleItemPosition();
                    int visiblePostCount = layoutManager.getChildCount();
                    int totalPostCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisiblePostPosition + visiblePostCount == totalPostCount)) {
                        isScrolling[0] = false;
                        getPostsNewsfeedSearch(currentQuery, false, postListViewModel, activity,
                                postsAdapter, postList, friends);
                    }
                }
            }
        };
        postsRecyclerView.addOnScrollListener(onScrollListener);
    }
}
