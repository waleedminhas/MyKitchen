package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.LIMIT;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Post;

/**
 * Live data class listens to user has scrolled enough new data needs to be fetched.
 */
public class PostListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private final Query query;
    private final OnLastVisiblePostCallback onLastVisiblePostCallback;
    private final OnLastPostReachedCallback onLastPostReachedCallback;
    private ListenerRegistration listenerRegistration;

    /**
     * @param query                     Firestore query
     * @param onLastVisiblePostCallback on last visible post callback
     * @param onLastPostReachedCallback on last post reached callback
     */
    PostListLiveData(Query query, OnLastVisiblePostCallback onLastVisiblePostCallback,
                     OnLastPostReachedCallback onLastPostReachedCallback) {
        this.query = query;
        this.onLastVisiblePostCallback = onLastVisiblePostCallback;
        this.onLastPostReachedCallback = onLastPostReachedCallback;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActive() {
        listenerRegistration = query.addSnapshotListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onInactive() {
        listenerRegistration.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                        @Nullable FirebaseFirestoreException e) {
        if (e != null) return;

        if (querySnapshot != null) {
            for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                switch (documentChange.getType()) {
                    case ADDED:
                        Post addedPost = documentChange.getDocument().toObject(Post.class);
                        Operation addOperation = new Operation(addedPost, R.string.added);
                        setValue(addOperation);
                        break;

                    case MODIFIED:
                        Post modifiedPost = documentChange.getDocument().toObject(Post.class);
                        Operation modifyOperation = new Operation(modifiedPost, R.string.modified);
                        setValue(modifyOperation);
                        break;

                    case REMOVED:
                        Post removedPost = documentChange.getDocument().toObject(Post.class);
                        Operation removeOperation = new Operation(removedPost, R.string.removed);
                        setValue(removeOperation);
                }
            }
        }

        int querySnapshotSize = 0;
        if (querySnapshot != null) {
            querySnapshotSize = querySnapshot.size();
        }
        if (querySnapshotSize < LIMIT) {
            onLastPostReachedCallback.setLastPostReached(true);
        } else {
            DocumentSnapshot lastVisiblePost =
                    querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisiblePostCallback.setLastVisiblePost(lastVisiblePost);
        }
    }

    /**
     * Interface for OnLastVisiblePostCallback, ensures setLastVisiblePost is implemented
     */
    interface OnLastVisiblePostCallback {
        void setLastVisiblePost(DocumentSnapshot lastVisiblePost);
    }

    /**
     * Interface for OnLastPostReachedCallback, ensures setLastPostReached is implemented
     */
    interface OnLastPostReachedCallback {
        void setLastPostReached(boolean isLastPostReached);
    }
}
