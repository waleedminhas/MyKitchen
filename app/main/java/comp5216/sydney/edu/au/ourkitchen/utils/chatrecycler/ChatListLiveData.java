package comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler;

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
public class ChatListLiveData extends LiveData<Operation> implements EventListener<QuerySnapshot> {
    private final Query query;
    private final OnLastVisibleChatCallback onLastVisibleChatCallback;
    private final OnLastChatReachedCallback onLastChatReachedCallback;
    private ListenerRegistration listenerRegistration;

    /**
     * @param query                     Firestore query
     * @param onLastVisibleChatCallback on last visible comment callback
     * @param onLastChatReachedCallback on last comment reached callback
     */
    ChatListLiveData(Query query, OnLastVisibleChatCallback onLastVisibleChatCallback,
                     OnLastChatReachedCallback onLastChatReachedCallback) {
        this.query = query;
        this.onLastVisibleChatCallback = onLastVisibleChatCallback;
        this.onLastChatReachedCallback = onLastChatReachedCallback;
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
                        User addedChat = documentChange.getDocument().toObject(User.class);
                        Operation addOperation = new Operation(addedChat, R.string.added);
                        setValue(addOperation);
                        break;

                    case MODIFIED:
                        User modifiedChat = documentChange.getDocument().toObject(User.class);
                        Operation modifyOperation = new Operation(modifiedChat,
                                R.string.modified);
                        setValue(modifyOperation);
                        break;

                    case REMOVED:
                        User removedChat = documentChange.getDocument().toObject(User.class);
                        Operation removeOperation = new Operation(removedChat, R.string.removed);
                        setValue(removeOperation);
                }
            }
        }

        int querySnapshotSize = 0;
        if (querySnapshot != null) {
            querySnapshotSize = querySnapshot.size();
        }
        if (querySnapshotSize < LIMIT) {
            onLastChatReachedCallback.setLastChatReached(true);
        } else {
            DocumentSnapshot lastVisibleChat =
                    querySnapshot.getDocuments().get(querySnapshotSize - 1);
            onLastVisibleChatCallback.setLastVisibleChat(lastVisibleChat);
        }
    }

    /**
     * Interface for last visible chat callback
     */
    interface OnLastVisibleChatCallback {
        void setLastVisibleChat(DocumentSnapshot lastVisibleChat);
    }

    /**
     * Interface for last chat reached callback
     */
    interface OnLastChatReachedCallback {
        void setLastChatReached(boolean isLastChatReached);
    }
}
