package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;

/**
 * Comment list view model class to store and manage the comments in a lifecycle conscious way.
 */
public class CommentListViewModel extends ViewModel {
    private CommentListRepository commentListRepository;
    private Query query;

    /**
     * Get the live data.
     *
     * @param query    Firestore query
     * @param newQuery true if this is a new query request, false otherwise
     * @return CommentListLiveData
     */
    public CommentListLiveData getCommentListLiveData(Query query, boolean newQuery) {
        if (this.query == null || !this.query.equals(query) || newQuery) {
            this.query = query;
            this.commentListRepository = new FirestoreCommentListRepository(query);
        }
        return commentListRepository.getCommentListLiveData();
    }

    /**
     * Comment list repository interface, ensures that there is a getCommentListLiveData() method.
     */
    interface CommentListRepository {
        CommentListLiveData getCommentListLiveData();
    }
}
