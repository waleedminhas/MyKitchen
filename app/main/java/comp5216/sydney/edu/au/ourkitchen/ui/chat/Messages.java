package comp5216.sydney.edu.au.ourkitchen.ui.chat;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.TIMESTAMP_FIELD;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Message;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler.InitScrollingMessages;
import comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler.MessageAdapter;
import comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler.MessageListViewModel;

/**
 * A simple {@link Fragment} subclass for searching for messages.
 */
public class Messages extends Fragment {
    private final List<Message> messagesList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private FirebaseUser currentUser;
    private String receiverID;
    private FirebaseFirestore mFirestore;
    private TextView noMatchingResults;
    private EditText messageInput;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messagesAdapter;
    private MessageListViewModel messageListViewModel;
    private LinearLayoutManager layoutManager;
    private CollectionReference messagesRef;
    private CollectionReference chatsRef;
    private String path;

    /**
     * An empty public constructor
     */
    public Messages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment message.
     */
    public static Messages newInstance() {
        Messages fragment = new Messages();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receiverID = getArguments().getString(Constants.USER_UID);
        }
    }

    /**
     * Hide the search option on this page
     *
     * @param menu menu
     */
    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        int[] ids = new int[]{R.id.search_icon, R.id.find_friends_icon};
        MenuItem item;
        for (int i : ids) {
            item = menu.findItem(i);
            if (item != null) {
                item.setVisible(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        messagesRef = mFirestore.collection(Constants.MESSAGE_COLLECTION);
        chatsRef = mFirestore.collection(Constants.CHAT_COLLECTION);
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messageInput = view.findViewById(R.id.message_input);
        Button postMessageBtn = view.findViewById(R.id.send_message);
        postMessageBtn.setOnClickListener(view1 -> sendMessage());

        noMatchingResults = view.findViewById(R.id.no_matching_results);
        messagesRecyclerView = view.findViewById(R.id.paging_recycler);

        initMessages(view);
        messagesRecyclerView.setVisibility(View.GONE);
        getMessages();
    }

    /**
     * Helper function, called when the user clicks the send message button. This will hide the
     * keyboard and upload the message to Firestore.
     */
    private void sendMessage() {
        // Hide keyboard when message button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        new Thread(() -> {
            String id = messagesRef.document().getId();

            if (currentUser.getUid().compareTo(receiverID) > 0)

                path = Integer.toString((currentUser.getUid() + receiverID).hashCode());
            else path = Integer.toString((receiverID + currentUser.getUid()).hashCode());


            Message message = new Message(id, currentUser.getUid(), receiverID,
                    new Timestamp(new Date()), messageInput.getText().toString());
            chatsRef.document(path).collection(Constants.MESSAGE_COLLECTION).document(id).set(message);
            messageInput.setText("");
        }).start();
    }

    /**
     * Gets the existing messages between the two users and displays them in the recycler view.
     */
    private void getMessages() {
        // Hide keyboard when message button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        // Convert string to Firestore query
        CollectionReference chatsRef = mFirestore.collection((Constants.CHAT_COLLECTION));
        Query query;

        if (currentUser.getUid().compareTo(receiverID) > 0) {
            query = chatsRef.document(Integer.toString((currentUser.getUid() + receiverID).hashCode())).collection(Constants.MESSAGE_COLLECTION).orderBy(TIMESTAMP_FIELD);
        } else {
            query = chatsRef.document(Integer.toString((receiverID + currentUser.getUid()).hashCode())).collection(Constants.MESSAGE_COLLECTION).orderBy(TIMESTAMP_FIELD);
        }

        executeQuery(query);
        messagesRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to execute the Firestore query and retrieve the information to display.
     *
     * @param query Firestore query
     */
    private void executeQuery(Query query) {
        messagesAdapter.clear();
        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(messagesRecyclerView,
                noMatchingResults);
        messagesAdapter.registerAdapterDataObserver(emptyDataObserver);

        InitScrollingMessages.getMessages(query, true, messageListViewModel, getActivity(),
                messagesAdapter, messagesList);

        InitScrollingMessages.initRecyclerViewOnScrollListener(isScrolling, query,
                messageListViewModel, getActivity(), messagesAdapter, messagesList,
                messagesRecyclerView, layoutManager);
    }

    /**
     * Helper function to initialise the fragment and find the views.
     *
     * @param view Fragment view
     */
    private void initMessages(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        messagesRecyclerView = view.findViewById(R.id.paging_recycler);
        messagesRecyclerView.setLayoutManager(layoutManager);

        messagesAdapter = new MessageAdapter(messagesList, getActivity());
        messagesRecyclerView.setAdapter(messagesAdapter);

        messageListViewModel = new ViewModelProvider(this).get(MessageListViewModel.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        messagesAdapter.clear();
    }
}
