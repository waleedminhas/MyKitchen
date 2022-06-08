package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Firestore user list repository to enable pagination of the query to save bandwidth. Will only
 * request a limited number of users when the last visible user has been reached.
 */
public class FirestoreFriendListRepository implements FriendListViewModel.FriendListRepository,
        FriendListLiveData.OnLastVisibleFriendCallback,
        FriendListLiveData.OnLastFriendReachedCallback {
    private Query query;
    private DocumentSnapshot lastVisibleFriend;
    private boolean isLastFriendReached;

    /**
     * Creates instance with Firestore query.
     *
     * @param query Firestore query
     */
    public FirestoreFriendListRepository(Query query) {
        this.query = query;
    }

    /**
     * Updates the live user list data. Checks whether the last user has been reached, or whether
     * another query needs to be created because we have reached the last visible user. Otherwise
     * returns the original query.
     *
     * @return live user list data
     */
    @Override
    public FriendListLiveData getFriendListLiveData() {
        if (isLastFriendReached) {
            return null;
        }
        if (lastVisibleFriend != null) {
            query = query.startAfter(lastVisibleFriend);
        }
        return new FriendListLiveData(query, this, this);
    }

    /**
     * Set the last visible user.
     *
     * @param lastVisibleFriend last visible friend on the user's screen
     */
    @Override
    public void setLastVisibleFriend(DocumentSnapshot lastVisibleFriend) {
        this.lastVisibleFriend = lastVisibleFriend;
    }

    /**
     * Set whether the last friend has been reached.
     *
     * @param isLastFriendReached true if last user reached, false otherwise
     */
    @Override
    public void setLastFriendReached(boolean isLastFriendReached) {
        this.isLastFriendReached = isLastFriendReached;
    }
}
