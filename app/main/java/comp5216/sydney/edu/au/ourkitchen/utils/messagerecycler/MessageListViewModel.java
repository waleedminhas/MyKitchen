package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;

/**
 * Message list view model class to store and manage the posts in a lifecycle conscious way.
 */
public class MessageListViewModel extends ViewModel {
    private MessageListRepository messageListRepository;
    private Query query;

    /**
     * Get the live data.
     *
     * @param query    Firestore query
     * @param newQuery true if this is a new query request, false otherwise
     * @return message list live data
     */
    public MessageListLiveData getMessageListLiveData(Query query, boolean newQuery) {
        if (this.query == null || !this.query.equals(query) || newQuery) {
            this.query = query;
            this.messageListRepository = new FirestoreMessageListRepository(query);
        }
        return messageListRepository.getMessageListLiveData();
    }

    /**
     * Interface for MessageListRepository, ensures getMessageListLiveData is implemented
     */
    interface MessageListRepository {
        MessageListLiveData getMessageListLiveData();
    }
}
