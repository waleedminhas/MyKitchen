package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Firestore post list repository to enable pagination of the query to save bandwidth. Will only
 * request a limited number of posts when the last visible post has been reached.
 */
public class FirestorePostListRepository implements PostListViewModel.PostListRepository,
        PostListLiveData.OnLastVisiblePostCallback, PostListLiveData.OnLastPostReachedCallback {
    private Query query;
    private DocumentSnapshot lastVisiblePost;
    private boolean isLastPostReached;

    /**
     * Creates instance with Firestore query.
     *
     * @param query Firestore query
     */
    public FirestorePostListRepository(Query query) {
        this.query = query;
    }

    /**
     * Updates the live post list data. Checks whether the last post has been reached, or whether
     * another query needs to be created because we have reached the last visible post. Otherwise
     * returns the original query.
     *
     * @return live post list data
     */
    @Override
    public PostListLiveData getPostListLiveData() {
        if (isLastPostReached) {
            return null;
        }
        if (lastVisiblePost != null) {
            query = query.startAfter(lastVisiblePost);
        }
        return new PostListLiveData(query, this, this);
    }

    /**
     * Set the last visible post.
     *
     * @param lastVisiblePost last visible post on the user's screen
     */
    @Override
    public void setLastVisiblePost(DocumentSnapshot lastVisiblePost) {
        this.lastVisiblePost = lastVisiblePost;
    }

    /**
     * Set whether the last post has been reached.
     *
     * @param isLastPostReached true if last post reached, false otherwise
     */
    @Override
    public void setLastPostReached(boolean isLastPostReached) {
        this.isLastPostReached = isLastPostReached;
    }
}