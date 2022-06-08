package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;

/**
 * Class for store information for FriendListView
 */
public class FriendListViewModel extends ViewModel {
    private FriendListRepository friendListRepository;
    private Query query;

    /**
     * Constructor
     *
     * @param query    An instance of Query to get friend list data
     * @param newQuery A flag to determine if new query is to be used
     * @return An instance of FriendListLiveData
     */
    public FriendListLiveData getFriendListLiveData(Query query, boolean newQuery) {
        if (this.query == null || !this.query.equals(query) || newQuery) {
            this.query = query;
            this.friendListRepository = new FirestoreFriendListRepository(query);
        }
        return friendListRepository.getFriendListLiveData();
    }

    /**
     * Interface for FriendListRepository, ensures getFriendListLiveData is implemented
     */
    interface FriendListRepository {
        FriendListLiveData getFriendListLiveData();
    }
}
