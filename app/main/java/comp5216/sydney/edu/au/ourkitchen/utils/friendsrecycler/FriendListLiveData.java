package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;

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
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * Live data class listens to user has scrolled enough new data needs to be fetched.
 */
public class FriendListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private final Query query;
    private final OnLastVisibleFriendCallback onLastVisibleFriendCallback;
    private final OnLastFriendReachedCallback onLastFriendReachedCallback;
    private ListenerRegistration listenerRegistration;

    /**
     * @param query                       Firestore query
     * @param onLastVisibleFriendCallback on last visible comment callback
     * @param onLastFriendReachedCallback on last comment reached callback
     */
    FriendListLiveData(Query query, OnLastVisibleFriendCallback onLastVisibleFriendCallback,
                       OnLastFriendReachedCallback onLastFriendReachedCallback) {
        this.query = query;
        this.onLastVisibleFriendCallback = onLastVisibleFriendCallback;
        this.onLastFriendReachedCallback = onLastFriendReachedCallback;
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
                        User addedFriend = documentChange.getDocument().toObject(User.class);
                        Operation addOperation = new Operation(addedFriend, R.string.added);
                        setValue(addOperation);
                        break;

                    case MODIFIED:
                        User modifiedFriend = documentChange.getDocument().toObject(User.class);
                        Operation modifyOperation = new Operation(modifiedFriend,
                                R.string.modified);
                        setValue(modifyOperation);
                        break;

                    case REMOVED:
                        User removedFriend = documentChange.getDocument().toObject(User.class);
                        Operation removeOperation = new Operation(removedFriend, R.string.removed);
                        setValue(removeOperation);
                }
            }
        }

        int querySnapshotSize = 0;
        if (querySnapshot != null) {
            querySnapshotSize = querySnapshot.size();
        }
        if (querySnapshotSize < LIMIT) {
            onLastFriendReachedCallback.setLastFriendReached(true);
        } else {
            DocumentSnapshot lastVisibleFriend =
                    querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisibleFriendCallback.setLastVisibleFriend(lastVisibleFriend);
        }
    }

    /**
     * Interface for OnLastVisibleFriendCallback
     */
    interface OnLastVisibleFriendCallback {
        void setLastVisibleFriend(DocumentSnapshot lastVisibleFriend);
    }

    /**
     * Interface for OnLastFriendReachedCallback
     */
    interface OnLastFriendReachedCallback {
        void setLastFriendReached(boolean isLastFriendReached);
    }
}
