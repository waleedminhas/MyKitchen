package comp5216.sydney.edu.au.ourkitchen.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.ChatListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.ChatsAdapter;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.InitScrollingChats;

/**
 * A simple {@link Fragment} subclass for displaying the friends. Users can click on a user to
 * start chat.
 */
public class Chat extends Fragment {
    private final List<User> usersList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private FirebaseUser currentUser;
    private FirebaseFirestore mFirestore;
    private TextView noMatchingResults;
    private RecyclerView chatsRecyclerView;
    private ChatsAdapter chatsAdapter;
    private ChatListViewModel chatListViewModel;
    private LinearLayoutManager layoutManager;

    /**
     * Required empty public constructor for fragment
     */
    public Chat() {

    }

    /**
     * Factory method creates a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Chat.
     */
    public static Chat newInstance() {
        Chat fragment = new Chat();
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
        item = menu.findItem(R.id.search_chat_icon);
        item.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatsRecyclerView = view.findViewById(R.id.paging_recycler);
        noMatchingResults = view.findViewById(R.id.no_matching_results);

        setUpListOfChats(view);
        chatsRecyclerView.setVisibility(View.GONE);

        setUpListOfFriends();
    }

    /**
     * Helper function to find the users friends and populate the recycler view.
     */
    private void setUpListOfFriends() {
        // Hide keyboard when comment button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        // Convert string to Firestore query
        CollectionReference chatsRef = mFirestore.collection(Constants.USERS_COLLECTION);

        mFirestore.collection(Constants.USERS_COLLECTION).document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.getFriends() != null && user.getFriends().size() > 0) {
                Query query = chatsRef.whereIn(Constants.USER_ID_FIELD, user.getFriends());
                executeQuery(query);
                chatsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Helper function to fill the adapter with the user's friends.
     *
     * @param query Firestore query of users
     */
    private void executeQuery(Query query) {
        chatsAdapter.clear();
        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(chatsRecyclerView,
                noMatchingResults);
        chatsAdapter.registerAdapterDataObserver(emptyDataObserver);

        InitScrollingChats.getChats(query, true, chatListViewModel, getActivity(), chatsAdapter,
                usersList);

        InitScrollingChats.initRecyclerViewOnScrollListener(isScrolling, query, chatListViewModel
                , getActivity(), chatsAdapter, usersList, chatsRecyclerView, layoutManager);
    }

    /**
     * Set up list of users to chat with.
     *
     * @param view fragment view
     */
    private void setUpListOfChats(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        chatsRecyclerView = view.findViewById(R.id.paging_recycler);
        chatsRecyclerView.setLayoutManager(layoutManager);

        chatsAdapter = new ChatsAdapter(usersList, getActivity());
        chatsRecyclerView.setAdapter(chatsAdapter);

        chatListViewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        chatsAdapter.clear();
    }
}
