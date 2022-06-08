package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;

import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * Helper class to initialise the friends and then make the recycler view responsive to scrolling
 */
public class InitScrollingFriends {
    /**
     * Helper method to get friend from the newsfeed, will observe changes in the firestore database
     *
     * @param query               Firestore query
     * @param isNewQuery          true if a new query, false otherwise
     * @param friendListViewModel friend list view model
     * @param activity            current activity
     * @param friendsAdapter      friend adapter
     * @param friendList          list of users
     */
    public static void getFriends(Query query, boolean isNewQuery,
                                  FriendListViewModel friendListViewModel,
                                  FragmentActivity activity, FriendsAdapter friendsAdapter,
                                  List<User> friendList) {
        FriendListLiveData friendListLiveData = friendListViewModel.getFriendListLiveData(query,
                isNewQuery);
        if (friendListLiveData != null && activity != null) {
            friendListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        User addedFriend = operation.user;
                        addFriend(addedFriend, friendList);
                        break;

                    case R.string.modified:
                        User modifiedFriend = operation.user;
                        modifyFriend(modifiedFriend, friendList);
                        break;

                    case R.string.removed:
                        User removedFriend = operation.user;
                        removeFriend(removedFriend, friendList);
                }
                friendsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Updates the friend list when a friend is added so the recycler view will update
     *
     * @param addedFriend friend to add to adapter
     * @param friendList  list of users
     */
    private static void addFriend(User addedFriend, List<User> friendList) {
        if (!addedFriend.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            friendList.add(addedFriend);
        }
    }

    /**
     * Updates the friend list when a friend is modified so the recycler view will update
     *
     * @param modifiedFriend a modified friend
     * @param friendList     list of users
     */
    private static void modifyFriend(User modifiedFriend, List<User> friendList) {
        for (int i = 0; i < friendList.size(); i++) {
            User currentFriend = friendList.get(i);
            if (modifiedFriend != null && currentFriend != null && currentFriend.getUid().equals(modifiedFriend.getUid())) {
                friendList.remove(currentFriend);
                friendList.add(i, modifiedFriend);
            }
        }
    }

    /**
     * Removes a friend from the friend list so it is reflected in the recycler view.
     *
     * @param removedFriend friend to remove from list
     * @param friendList    list of users
     */
    private static void removeFriend(User removedFriend, List<User> friendList) {
        for (int i = 0; i < friendList.size(); i++) {
            User currentFriend = friendList.get(i);
            if (currentFriend.getUid().equals(removedFriend.getUid())) {
                friendList.remove(currentFriend);
            }
        }
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling         true if the users is scrolling, false otherwise
     * @param currentQuery        Firestore query
     * @param friendListViewModel friend list view model
     * @param activity            current activity
     * @param friendsAdapter      friend adapter
     * @param friendList          list of users
     * @param friendsRecyclerView recycler view
     * @param layoutManager       layout manager
     */
    public static void initRecyclerViewOnScrollListener(boolean[] isScrolling, Query currentQuery
            , FriendListViewModel friendListViewModel, FragmentActivity activity,
                                                        FriendsAdapter friendsAdapter,
                                                        List<User> friendList,
                                                        RecyclerView friendsRecyclerView,
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
                    int firstVisibleFriendPosition = layoutManager.findFirstVisibleItemPosition();
                    int visibleFriendCount = layoutManager.getChildCount();
                    int totalFriendCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisibleFriendPosition + visibleFriendCount == totalFriendCount)) {
                        isScrolling[0] = false;
                        getFriends(currentQuery, false, friendListViewModel, activity,
                                friendsAdapter, friendList);
                    }
                }
            }
        };
        friendsRecyclerView.addOnScrollListener(onScrollListener);
    }
}
