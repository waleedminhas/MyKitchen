package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;

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
import comp5216.sydney.edu.au.ourkitchen.model.Message;

/**
 * Live data class listens to user has scrolled enough new data needs to be fetched.
 */
public class MessageListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private final Query query;
    private final OnLastVisibleMessageCallback onLastVisibleMessageCallback;
    private final OnLastMessageReachedCallback onLastMessageReachedCallback;
    private ListenerRegistration listenerRegistration;

    /**
     * @param query                        Firestore query
     * @param onLastVisibleMessageCallback on last visible comment callback
     * @param onLastMessageReachedCallback on last comment reached callback
     */
    MessageListLiveData(Query query, OnLastVisibleMessageCallback onLastVisibleMessageCallback,
                        OnLastMessageReachedCallback onLastMessageReachedCallback) {
        this.query = query;
        this.onLastVisibleMessageCallback = onLastVisibleMessageCallback;
        this.onLastMessageReachedCallback = onLastMessageReachedCallback;
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
                        Message addedMessage = documentChange.getDocument().toObject(Message.class);
                        Operation addOperation = new Operation(addedMessage, R.string.added);
                        setValue(addOperation);
                        break;

                    case MODIFIED:
                        Message modifiedMessage = documentChange.getDocument().toObject(Message.class);
                        Operation modifyOperation = new Operation(modifiedMessage,
                                R.string.modified);
                        setValue(modifyOperation);
                        break;

                    case REMOVED:
                        Message removedMessage = documentChange.getDocument().toObject(Message.class);
                        Operation removeOperation = new Operation(removedMessage, R.string.removed);
                        setValue(removeOperation);
                }
            }
        }

        int querySnapshotSize = 0;
        if (querySnapshot != null) {
            querySnapshotSize = querySnapshot.size();
        }
        if (querySnapshotSize < LIMIT) {
            onLastMessageReachedCallback.setLastMessageReached(true);
        } else {
            DocumentSnapshot lastVisibleMessage =
                    querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisibleMessageCallback.setLastVisibleMessage(lastVisibleMessage);
        }
    }

    /**
     * Interface for OnLastVisibleMessageCallback, ensures setLastVisibleMessage is implemented
     */
    interface OnLastVisibleMessageCallback {
        void setLastVisibleMessage(DocumentSnapshot lastVisibleMessage);
    }

    /**
     * Interface for OnLastMessageReachedCallback, ensures setLastMessageReached is implemented
     */
    interface OnLastMessageReachedCallback {
        void setLastMessageReached(boolean isLastMessageReached);
    }
}
