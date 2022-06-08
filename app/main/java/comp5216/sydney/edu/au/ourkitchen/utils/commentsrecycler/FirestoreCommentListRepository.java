package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Firestore comment list repository to enable pagination of the query to save bandwidth. Will only
 * request a limited number of comments when the last visible comment has been reached.
 */
public class FirestoreCommentListRepository implements CommentListViewModel.CommentListRepository
        , CommentListLiveData.OnLastVisibleCommentCallback,
        CommentListLiveData.OnLastCommentReachedCallback {
    private Query query;
    private DocumentSnapshot lastVisibleComment;
    private boolean isLastCommentReached;

    /**
     * Creates instance with Firestore query.
     *
     * @param query Firestore query
     */
    public FirestoreCommentListRepository(Query query) {
        this.query = query;
    }

    /**
     * Updates the live comment list data. Checks whether the last comment has been reached, or whether
     * another query needs to be created because we have reached the last visible comment. Otherwise
     * returns the original query.
     *
     * @return live comment list data
     */
    @Override
    public CommentListLiveData getCommentListLiveData() {
        if (isLastCommentReached) {
            return null;
        }
        if (lastVisibleComment != null) {
            query = query.startAfter(lastVisibleComment);
        }
        return new CommentListLiveData(query, this, this);
    }

    /**
     * Set the last visible comment.
     *
     * @param lastVisibleComment last visible comment on the user's screen
     */
    @Override
    public void setLastVisibleComment(DocumentSnapshot lastVisibleComment) {
        this.lastVisibleComment = lastVisibleComment;
    }

    /**
     * Set whether the last comment has been reached.
     *
     * @param isLastCommentReached true if last comment reached, false otherwise
     */
    @Override
    public void setLastCommentReached(boolean isLastCommentReached) {
        this.isLastCommentReached = isLastCommentReached;
    }
}

