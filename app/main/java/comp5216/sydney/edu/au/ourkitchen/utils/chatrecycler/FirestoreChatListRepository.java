package comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler;


import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Firestore chat    list repository to enable pagination of the query to save bandwidth. Will only
 * request a limited number of posts when the last visible post has been reached.
 */
public class FirestoreChatListRepository implements ChatListViewModel.ChatListRepository,
        ChatListLiveData.OnLastVisibleChatCallback,
        ChatListLiveData.OnLastChatReachedCallback {
    private Query query;
    private DocumentSnapshot lastVisibleChat;
    private boolean isLastChatReached;

    /**
     * Construct new instance.
     *
     * @param query Firestore query
     */
    public FirestoreChatListRepository(Query query) {
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatListLiveData getChatListLiveData() {
        if (isLastChatReached) {
            return null;
        }
        if (lastVisibleChat != null) {
            query = query.startAfter(lastVisibleChat);
        }
        return new ChatListLiveData(query, this, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastVisibleChat(DocumentSnapshot lastVisibleChat) {
        this.lastVisibleChat = lastVisibleChat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastChatReached(boolean isLastChatReached) {
        this.isLastChatReached = isLastChatReached;
    }
}
