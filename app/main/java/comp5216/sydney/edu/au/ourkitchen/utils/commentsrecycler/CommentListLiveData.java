package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;

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
import comp5216.sydney.edu.au.ourkitchen.model.Comment;

/**
 * Live data class listens to user has scrolled enough new data needs to be fetched.
 */
public class CommentListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private final Query query;
    private final OnLastVisibleCommentCallback onLastVisibleCommentCallback;
    private final OnLastCommentReachedCallback onLastCommentReachedCallback;
    private ListenerRegistration listenerRegistration;

    /**
     * @param query                        Firestore query
     * @param onLastVisibleCommentCallback on last visible comment callback
     * @param onLastCommentReachedCallback on last comment reached callback
     */
    CommentListLiveData(Query query, OnLastVisibleCommentCallback onLastVisibleCommentCallback,
                        OnLastCommentReachedCallback onLastCommentReachedCallback) {
        this.query = query;
        this.onLastVisibleCommentCallback = onLastVisibleCommentCallback;
        this.onLastCommentReachedCallback = onLastCommentReachedCallback;
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
                        Comment addedComment = documentChange.getDocument().toObject(Comment.class);
                        Operation addOperation = new Operation(addedComment, R.string.added);
                        setValue(addOperation);
                        break;

                    case MODIFIED:
                        Comment modifiedComment =
                                documentChange.getDocument().toObject(Comment.class);
                        Operation modifyOperation = new Operation(modifiedComment,
                                R.string.modified);
                        setValue(modifyOperation);
                        break;

                    case REMOVED:
                        Comment removedComment =
                                documentChange.getDocument().toObject(Comment.class);
                        Operation removeOperation = new Operation(removedComment, R.string.removed);
                        setValue(removeOperation);
                }
            }
        }

        int querySnapshotSize = 0;
        if (querySnapshot != null) {
            querySnapshotSize = querySnapshot.size();
        }
        if (querySnapshotSize < LIMIT) {
            onLastCommentReachedCallback.setLastCommentReached(true);
        } else {
            DocumentSnapshot lastVisibleComment =
                    querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisibleCommentCallback.setLastVisibleComment(lastVisibleComment);
        }
    }

    /**
     * Interface for OnLastVisibleCommentCallback
     */
    interface OnLastVisibleCommentCallback {
        void setLastVisibleComment(DocumentSnapshot lastVisibleComment);
    }

    /**
     * Interface for OnLastCommentReachedCallback
     */
    interface OnLastCommentReachedCallback {
        void setLastCommentReached(boolean isLastCommentReached);
    }
}
