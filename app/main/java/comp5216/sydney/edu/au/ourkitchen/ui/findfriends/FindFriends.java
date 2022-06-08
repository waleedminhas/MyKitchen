package comp5216.sydney.edu.au.ourkitchen.ui.findfriends;

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
import android.widget.RadioButton;
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
import comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler.FriendListViewModel;
import comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler.FriendsAdapter;
import comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler.InitScrollingFriends;

/**
 * A simple {@link Fragment} subclass for FindFriends. Enables the user to search for other chefs
 * by interests or by their name.
 */
public class FindFriends extends Fragment {
    // Views for the recycler view
    private final List<User> friendList = new ArrayList<>();
    private final boolean[] isScrolling = new boolean[1];
    private EditText searchBar;
    private TextView noMatchingResults;
    private RadioButton viaName, viaInterests;
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private FriendListViewModel friendListViewModel;
    private LinearLayoutManager layoutManager;

    private FirebaseFirestore mFirestore;

    /**
     * Empty constructor for FindFriends class
     */
    public FindFriends() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of the FindFriends fragment.
     *
     * @return A new instance of fragment FindFriends.
     */
    public static FindFriends newInstance() {
        FindFriends fragment = new FindFriends();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFirestore = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    /**
     * Hide the search option on this page
     *
     * @param menu top menu
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        setUpListOfFriends(view);
        friendsRecyclerView.setVisibility(View.GONE);
    }

    /**
     * Helper function to find all the views in the fragment.
     *
     * @param view fragment view
     */
    private void findViews(View view) {
        searchBar = view.findViewById(R.id.search_friends_bar);

        ImageButton searchBtn = view.findViewById(R.id.search_friends_action);
        searchBtn.setOnClickListener(view1 -> searchForFriends());

        friendsRecyclerView = view.findViewById(R.id.paging_recycler);

        viaInterests = view.findViewById(R.id.search_by_interests_radio);
        viaInterests.setOnClickListener(view12 -> {
            viaInterests.setChecked(true);
            viaName.setChecked(false);
        });
        viaName = view.findViewById(R.id.search_by_name_radio);
        viaName.setOnClickListener(view13 -> {
            viaInterests.setChecked(false);
            viaName.setChecked(true);
        });
        noMatchingResults = view.findViewById(R.id.no_matching_results);
    }

    /**
     * Helper function to set up the list of friends recycler view.
     *
     * @param view fragment view
     */
    private void setUpListOfFriends(View view) {
        layoutManager = new LinearLayoutManager(getActivity());
        friendsRecyclerView = view.findViewById(R.id.paging_recycler);
        friendsRecyclerView.setLayoutManager(layoutManager);

        friendsAdapter = new FriendsAdapter(friendList, getActivity());
        friendsRecyclerView.setAdapter(friendsAdapter);

        friendListViewModel = new ViewModelProvider(this).get(FriendListViewModel.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
        friendsAdapter.clear();
    }

    /**
     * Function executed when search button clicked to find friends.
     */
    private void searchForFriends() {
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
            CollectionReference friendsRef = mFirestore.collection(Constants.USERS_COLLECTION);

            Query query;
            List<String> separateTerms = Arrays.asList(stringQuery.split(" "));
            separateTerms =
                    separateTerms.stream().map(String::toLowerCase).collect(Collectors.toList());

            if (viaName.isChecked()) {
                separateTerms.add(stringQuery.toLowerCase());
                query = friendsRef.whereArrayContainsAny(Constants.USER_NAME_CASE_INSENSITIVE_FIELD,
                        separateTerms).limit(Constants.LIMIT);
            } else {
                query = friendsRef.whereArrayContainsAny(Constants.USER_INTERESTS_FIELD,
                        separateTerms).limit(Constants.LIMIT);
            }

            executeQuery(query);
            friendsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            Utils.showToast(getActivity(), getString(R.string.enter_a_search_term));
        }
    }

    /**
     * Executes the query and initialises the adapter with matching friends and scrolling listener.
     *
     * @param query Firestore query
     */
    private void executeQuery(Query query) {
        friendsAdapter.clear();
        EmptyDataObserver emptyDataObserver = new EmptyDataObserver(friendsRecyclerView,
                noMatchingResults);
        friendsAdapter.registerAdapterDataObserver(emptyDataObserver);

        InitScrollingFriends.getFriends(query, true, friendListViewModel, getActivity(),
                friendsAdapter, friendList);

        InitScrollingFriends.initRecyclerViewOnScrollListener(isScrolling, query,
                friendListViewModel, getActivity(), friendsAdapter, friendList,
                friendsRecyclerView, layoutManager);
    }
}
