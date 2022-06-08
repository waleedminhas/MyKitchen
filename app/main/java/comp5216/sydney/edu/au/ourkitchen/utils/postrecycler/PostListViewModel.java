package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;

/**
 * Post list view model class to store and manage the posts in a lifecycle conscious way.
 */
public class PostListViewModel extends ViewModel {
    private PostListRepository postListRepository;
    private Query query;

    /**
     * Get the live data.
     *
     * @param query    Firestore query
     * @param newQuery true if this is a new query request, false otherwise
     * @return post list live data
     */
    public PostListLiveData getPostListLiveData(Query query, boolean newQuery) {
        if (this.query == null || !this.query.equals(query) || newQuery) {
            this.query = query;
            this.postListRepository = new FirestorePostListRepository(query);
        }
        return postListRepository.getPostListLiveData();
    }

    /**
     * Refresh the data by querying Firestore again.
     */
    public void refreshData() {
        this.postListRepository = new FirestorePostListRepository(this.query);
    }

    /**
     * Post list repository interface, ensures that there is a getPostListLiveData() method.
     */
    interface PostListRepository {
        PostListLiveData getPostListLiveData();
    }
}
