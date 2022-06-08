package comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.Query;

public class ChatListViewModel extends ViewModel {
    private ChatListRepository chatListRepository;
    private Query query;

    /**
     * Function to get the ChatListLiveData
     *
     * @param query    An instance of Query to get the chat list data
     * @param newQuery A flag to determine if new query should be used
     * @return An instance of ChatListLiveData
     */
    public ChatListLiveData getChatListLiveData(Query query, boolean newQuery) {
        if (this.query == null || !this.query.equals(query) || newQuery) {
            this.query = query;
            this.chatListRepository = new FirestoreChatListRepository(query);
        }
        return chatListRepository.getChatListLiveData();
    }

    /**
     * Interface for ChatListRepository, stores chat list live data
     */
    interface ChatListRepository {
        ChatListLiveData getChatListLiveData();
    }
}
