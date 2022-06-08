package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.FRIEND_REQUESTS_COLLECTION;
import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.FRIEND_REQUEST_FRIENDS;
import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.FRIEND_REQUEST_STATUS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.FriendRequest;
import comp5216.sydney.edu.au.ourkitchen.model.FriendRequestStatus;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * PendingFriendRequests extends {@link Fragment}.
 * This class displays a list of pending friend requests to the user that they can accept or deny.
 */
public class PendingFriendRequests extends Fragment {
    private static final String TAG = "PendingFriendRequests";

    List<FriendRequest> pendingRequestsList;
    PendingFriendRequestsAdapter adapter;
    private TextView noRequestsTxt;
    private FirebaseFirestore mStorage;
    PendingFriendRequestsAdapter.OnAcceptFriendRequestClick onAccept = this::acceptFriendRequest;
    PendingFriendRequestsAdapter.OnDenyFriendRequestClick onDeny = this::denyFriendRequest;
    private String userId;

    /**
     * Constructor
     */
    public PendingFriendRequests() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of this fragment
     *
     * @return newly created fragment
     */
    public static PendingFriendRequests newInstance() {
        PendingFriendRequests fragment = new PendingFriendRequests();
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_friend_requests, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (pendingRequestsList == null) {
            pendingRequestsList = new ArrayList<>();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        loadPendingFriendRequests();
        setupRecyclerView(view);

        noRequestsTxt = view.findViewById(R.id.noRequests);
    }

    /**
     * loads the pending requests for this user
     */
    private void loadPendingFriendRequests() {

        DocumentReference documentReference =
                mStorage.collection(Constants.USERS_COLLECTION).document(userId);
        documentReference.addSnapshotListener((value, error) -> {
            pendingRequestsList.clear();

            if (value != null) {
                User user = value.toObject(User.class);
                if (user != null) {
                    List<String> friendRequests = user.getFriendRequests();

                    /*
                     for each friend request id get the firestore FriendRequest and check if
                     pending.
                    */
                    if (friendRequests != null && friendRequests.size() != 0) {
                        friendRequests.forEach((requestId) -> {
                            DocumentReference requestRef =
                                    mStorage.collection("friendRequests").document(requestId);
                            requestRef.addSnapshotListener((value1, error1) -> {
                                if (value1 != null) {
                                    FriendRequest fr = value1.toObject(FriendRequest.class);
                                    if (fr != null) {
                                        if (fr.getStatus().equals(FriendRequestStatus.PENDING.name()) && fr.getFriendToId().equals(userId)) {
                                            pendingRequestsList.add(fr);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }

                                    if (pendingRequestsList.size() == 0) {
                                        noRequestsTxt.setVisibility(View.VISIBLE);
                                    } else {
                                        noRequestsTxt.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        });

                    } else {
                        noRequestsTxt.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * Accepts a friend request.
     * Updates the friend request, and the friends list of both users.
     *
     * @param friendRequest the friend request
     */
    private void acceptFriendRequest(FriendRequest friendRequest) {
        // update status to 'approved"
        String userId = FirebaseAuth.getInstance().getUid();

        String id = friendRequest.getId();
        if (id != null) {
            DocumentReference friendRequestRef = mStorage.collection(FRIEND_REQUESTS_COLLECTION).document(id);
            friendRequestRef.update(FRIEND_REQUEST_STATUS, FriendRequestStatus.APPROVED.name());

            // update this user to have this requesting user as a friend
            if (userId != null) {
                DocumentReference userDocRef =
                        FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION).document(userId);
                userDocRef.update(FRIEND_REQUEST_FRIENDS, FieldValue.arrayUnion(friendRequest.getFriendFromId()));

                // update the requesting user to have this person as a friend.
                DocumentReference otherUser =
                        FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION).document(friendRequest.getFriendFromId());
                otherUser.update(FRIEND_REQUEST_FRIENDS, FieldValue.arrayUnion(userId));

                pendingRequestsList.remove(friendRequest);
                adapter.notifyDataSetChanged();
            }
        }
    }


    /**
     * Denies a friend request.
     * Updates the friend request status.
     *
     * @param friendRequest the friend request to dney
     */
    private void denyFriendRequest(FriendRequest friendRequest) {
        // update status to denied
        String id = friendRequest.getId();
        if (id != null) {
            DocumentReference friendRequestRef = mStorage.collection("friendRequests").document(id);
            friendRequestRef.update(FRIEND_REQUEST_STATUS, FriendRequestStatus.DENIED.name());
            pendingRequestsList.remove(friendRequest);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets up a recycler view for the pending requests.
     *
     * @param view fragment view
     */
    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.requests_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new PendingFriendRequestsAdapter(pendingRequestsList, getContext(), onAccept,
                onDeny);
        recyclerView.setAdapter(adapter);
    }
}
