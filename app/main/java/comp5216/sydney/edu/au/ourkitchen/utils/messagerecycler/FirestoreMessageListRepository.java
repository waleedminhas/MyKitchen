package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Class to store Firestore Message Lists
 */
public class FirestoreMessageListRepository implements MessageListViewModel.MessageListRepository,
        MessageListLiveData.OnLastVisibleMessageCallback,
        MessageListLiveData.OnLastMessageReachedCallback {
    private Query query;
    private DocumentSnapshot lastVisibleMessage;
    private boolean isLastMessageReached;

    /**
     * Constructor
     *
     * @param query An instance of Query to get Message Lists
     */
    public FirestoreMessageListRepository(Query query) {
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListLiveData getMessageListLiveData() {
        if (isLastMessageReached) {
            return null;
        }
        if (lastVisibleMessage != null) {
            query = query.startAfter(lastVisibleMessage);
        }
        return new MessageListLiveData(query, this, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastVisibleMessage(DocumentSnapshot lastVisibleMessage) {
        this.lastVisibleMessage = lastVisibleMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastMessageReached(boolean isLastMessageReached) {
        this.isLastMessageReached = isLastMessageReached;
    }
}
