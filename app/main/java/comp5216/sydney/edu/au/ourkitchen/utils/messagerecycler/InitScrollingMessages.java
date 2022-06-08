package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;

import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Message;

/**
 * A class to store messages list
 */
public class InitScrollingMessages {
    /**
     * Helper method to get message from the newsfeed, will observe changes in the firestore database
     *
     * @param query                The query to fetch messages
     * @param isNewQuery           A flag to use current query or previous query
     * @param messageListViewModel An instance of MessageListViewModel
     * @param activity             An instance of FragmentActivity
     * @param messageAdapter       An instance of MessageAdapter
     * @param messageList          A List of Messages
     */
    public static void getMessages(Query query, boolean isNewQuery,
                                   MessageListViewModel messageListViewModel,
                                   FragmentActivity activity, MessageAdapter messageAdapter,
                                   List<Message> messageList) {
        MessageListLiveData messageListLiveData = messageListViewModel.getMessageListLiveData(query,
                isNewQuery);
        if (messageListLiveData != null && activity != null) {
            messageListLiveData.observe(activity, operation -> {
                switch (operation.type) {
                    case R.string.added:
                        Message addedMessage = operation.message;
                        addMessage(addedMessage, messageList);
                        break;

                    case R.string.modified:
                        Message modifiedMessage = operation.message;
                        modifyMessage(modifiedMessage, messageList);
                        break;

                    case R.string.removed:
                        Message removedMessage = operation.message;
                        removeMessage(removedMessage, messageList);
                }
                messageAdapter.notifyDataSetChanged();
            });
        }
    }

    /**
     * Updates the message list when a message is added so the recycler view will update
     *
     * @param addedMessage An instance of Message
     * @param messageList  The List of existing Messages
     */
    private static void addMessage(Message addedMessage, List<Message> messageList) {
        messageList.add(addedMessage);
    }

    /**
     * Updates the message list when a message is modified so the recycler view will update
     *
     * @param modifiedMessage a modified message
     * @return void
     */
    private static void modifyMessage(Message modifiedMessage, List<Message> messageList) {
        for (int i = 0; i < messageList.size(); i++) {
            Message currentMessage = messageList.get(i);
            if (modifiedMessage != null && currentMessage != null && currentMessage.getId().equals(modifiedMessage.getId())) {
                messageList.remove(currentMessage);
                messageList.add(i, modifiedMessage);
            }
        }
    }

    /**
     * Removes a message from the message list so it is reflected in the recycler view.
     *
     * @param removedMessage The instance of Message to be removed
     * @param messageList    The List of existing Messages
     */
    private static void removeMessage(Message removedMessage, List<Message> messageList) {
        for (int i = 0; i < messageList.size(); i++) {
            Message currentMessage = messageList.get(i);
            if (currentMessage.getId().equals(removedMessage.getId())) {
                messageList.remove(currentMessage);
            }
        }
    }

    /**
     * Helper function to initialise the recycler view to be responsive to scrolling
     *
     * @param isScrolling          A flag to determine if recycler view is scrolling
     * @param currentQuery         The query currently in use
     * @param messageListViewModel An instance of MessageListViewModel
     * @param activity             An instance of FragmentActivity
     * @param messageAdapter       An instance of MessageAdapter
     * @param messageList          A List of Messages
     * @param messagesRecyclerView An instance of RecyclerView used for messages
     * @param layoutManager        An instance of LinearLayoutManager
     */
    public static void initRecyclerViewOnScrollListener(boolean[] isScrolling, Query currentQuery
            , MessageListViewModel messageListViewModel, FragmentActivity activity,
                                                        MessageAdapter messageAdapter,
                                                        List<Message> messageList,
                                                        RecyclerView messagesRecyclerView,
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
                    int firstVisibleMessagePosition = layoutManager.findFirstVisibleItemPosition();
                    int visibleMessageCount = layoutManager.getChildCount();
                    int totalMessageCount = layoutManager.getItemCount();

                    if (isScrolling[0] && (firstVisibleMessagePosition + visibleMessageCount == totalMessageCount)) {
                        isScrolling[0] = false;
                        getMessages(currentQuery, false, messageListViewModel, activity,
                                messageAdapter, messageList);
                    }
                }
            }
        };
        messagesRecyclerView.addOnScrollListener(onScrollListener);
    }
}
