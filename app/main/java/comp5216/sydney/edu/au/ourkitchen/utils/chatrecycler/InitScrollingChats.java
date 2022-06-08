package comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler;

import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

public class InitScrollingChats {
    /**
     * Helper method to get chat from the newsfeed, will observe changes in the firestore database
     *
     * @param query             The query to fetch chats
     * @param isNewQuery        Flag to reuse existing query
     * @param chatListViewModel An instance of the chatListViewModel
     * @param activity          An instance of FragmentActivity
     * @param chatsAdapter      An instance of ChatsAdapter
     * @param chatList          A List of Users
     */
    public static void getChats(Query query, boolean isNewQuery,
                                ChatListViewModel chatListViewModel,
                                FragmentActivity activity, ChatsAdapter chatsAdapter,
                                List<User> chatList) {
        ChatListLiveData chatListLiveData = chatListViewModel.getChatListLiveData(query,
                isNewQuery);
        if (chatListLiveData != null && activity != null) {
            chatListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        User addedChat = operation.user;
                        addChat(addedChat, chatList);
                        break;

                    case R.string.modified:
                        User modifiedChat = operation.user;
                        modifyChat(modifiedChat, chatList);
                        break;

                    case R.string.removed:
                        User removedChat = operation.user;
                        removeChat(removedChat, chatList);
                }
                chatsAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Updates the chat list when a chat is added so the recycler view will update
     *
     * @param addedChat A User instance
     * @param chatList  A List of Users
     */
    private static void addChat(User addedChat, List<User> chatList) {
        chatList.add(addedChat);
    }

    /**
     * Updates the chat list when a chat is modified so the recycler view will update
     *
     * @param modifiedChat a modified chat
     * @return void
     */
    private static void modifyChat(User modifiedChat, List<User> chatList) {
        for (int i = 0; i < chatList.size(); i++) {
            User currentChat = chatList.get(i);
            if (modifiedChat != null && currentChat != null && currentChat.getUid().equals(modifiedChat.getUid())) {
                chatList.remove(currentChat);
                chatList.add(i, modifiedChat);
            }
        }
    }

    /**
     * Removes a chat from the chat list so it is reflected in the recycler view.
     *
     * @param removedChat A User instance
     * @param chatList    A List of Users
     */
    private static void removeChat(User removedChat, List<User> chatList) {
        for (int i = 0; i < chatList.size(); i++) {
            User currentChat = chatList.get(i);
            if (currentChat.getUid().equals(removedChat.getUid())) {
                chatList.remove(currentChat);
            }
        }
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling       A flag to determine if RecyclerView is scrolling
     * @param currentQuery      The query in use currently
     * @param chatListViewModel An instance of chatListViewModel
     * @param activity          An instance of FragmentActivity
     * @param chatsAdapter      An instance of ChatsAdapter
     * @param chatList          A List of Users
     * @param chatsRecyclerView An instance of RecyclerView for chats
     * @param layoutManager     An instance of LinearLayoutManager
     */
    public static void initRecyclerViewOnScrollListener(boolean[] isScrolling, Query currentQuery
            , ChatListViewModel chatListViewModel, FragmentActivity activity,
                                                        ChatsAdapter chatsAdapter,
                                                        List<User> chatList,
                                                        RecyclerView chatsRecyclerView,
                                                        LinearLayoutManager layoutManager) {
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling[0] = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager != null) {
                    int firstVisibleChatPosition = layoutManager.findFirstVisibleItemPosition();
                    int visibleChatCount = layoutManager.getChildCount();
                    int totalChatCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisibleChatPosition + visibleChatCount == totalChatCount)) {
                        isScrolling[0] = false;
                        getChats(currentQuery, false, chatListViewModel, activity,
                                chatsAdapter, chatList);
                    }
                }
            }
        };
        chatsRecyclerView.addOnScrollListener(onScrollListener);
    }
}
