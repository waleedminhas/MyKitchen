package comp5216.sydney.edu.au.ourkitchen.ui.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;
import comp5216.sydney.edu.au.ourkitchen.utils.EmptyDataObserver;
import comp5216.sydney.edu.au.ourkitchen.utils.Utils;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.ChatListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.ChatsAdapter;
import comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler.InitScrollingChats;

/**
 * A simple {@link Fragment} subclass for searching for friends to chat with.
 */
public class ChatSearch extends Fragment {
    private final List<User> userList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private FirebaseFirestore mFirestore;
    private EditText searchBar;
    private TextView noMatchingResults;
    private RecyclerView userRecyclerView;
    private ChatsAdapter chatsAdapter;
    private ChatListViewModel chatListViewModel;
    private LinearLayoutManager layoutManager;

    /**
     * Required empty public constructor for fragment
     */
    public ChatSearch() {
    }

    /**
     * Factory method creates a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatSearch.
     */
    public static ChatSearch newInstance() {
        ChatSearch fragment = new ChatSearch();
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFirestore = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_chat_search, container, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        setUpListOfUsers(view);
        userRecyclerView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    private void findViews(View view) {
        searchBar = view.findViewById(R.id.search_chat_bar);

        ImageButton searchBtn = view.findViewById(R.id.search_chat_action);
        searchBtn.setOnClickListener(view1 -> searchForUsers());

        userRecyclerView = view.findViewById(R.id.paging_recycler);
        noMatchingResults = view.findViewById(R.id.no_matching_results);
    }

    /**
     * {@inheritDoc}
     */
    private void setUpListOfUsers(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        userRecyclerView = view.findViewById(R.id.paging_recycler);
        userRecyclerView.setLayoutManager(layoutManager);

        chatsAdapter = new ChatsAdapter(userList, getActivity());
        userRecyclerView.setAdapter(chatsAdapter);

        chatListViewModel = new ViewModelProvider(this).get(ChatListViewModel.class);
    }

    /**
     * Called when the user clicks search. Will find the matching users based no their entered
     * search term.
     */
    private void searchForUsers() {
        // Hide keyboard when comment button clicked
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }

        // Convert string to Firestore query
        String stringQuery = searchBar.getText().toString();
        Utils.showToast(getActivity(), stringQuery);
        if (!stringQuery.equals("")) {
            CollectionReference chatsRef = mFirestore.collection(Constants.USERS_COLLECTION);

            Query query;
            List<String> separateTerms = Arrays.asList(stringQuery.split(" "));
            separateTerms =
                    separateTerms.stream().map(String::toLowerCase).collect(Collectors.toList());

            query =
                    chatsRef.whereArrayContainsAny(Constants.USER_NAME_CASE_INSENSITIVE_FIELD,
                            separateTerms).limit(Constants.LIMIT);

            executeQuery(query);
            userRecyclerView.setVisibility(View.VISIBLE);
        } else {
            Utils.showToast(getActivity(), getString(R.string.enter_a_search_term));
        }
    }

    /**
     * Helper function to execute the user query and update the search results
     *
     * @param query Firestore search query
     */
    private void executeQuery(Query query) {
        chatsAdapter.clear();
        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(userRecyclerView,
                noMatchingResults);
        chatsAdapter.registerAdapterDataObserver(emptyDataObserver);

        InitScrollingChats.getChats(query, true, chatListViewModel, getActivity(), chatsAdapter,
                userList);

        InitScrollingChats.initRecyclerViewOnScrollListener(isScrolling, query, chatListViewModel
                , getActivity(), chatsAdapter, userList, userRecyclerView, layoutManager);
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
