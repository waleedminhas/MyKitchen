package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.FriendRequest;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * PendingFriendRequestsAdapter extends {@link RecyclerView.Adapter}.
 * Used as an adapter for a list of pending friend requests.
 */
public class PendingFriendRequestsAdapter extends RecyclerView.Adapter<PendingFriendRequestsAdapter.ViewHolder> {
    private final List<FriendRequest> requestsList;
    private final Context ctx;
    private final OnAcceptFriendRequestClick onAcceptFriendRequestClick;
    private final OnDenyFriendRequestClick onDenyFriendRequestClick;

    /**
     * Constructor
     *
     * @param requestsList list of friend request objects
     */
    public PendingFriendRequestsAdapter(List<FriendRequest> requestsList, Context context,
                                        OnAcceptFriendRequestClick onAcceptFriendRequestClick,
                                        OnDenyFriendRequestClick onDenyFriendRequestClick) {
        this.requestsList = requestsList;
        this.ctx = context;
        this.onAcceptFriendRequestClick = onAcceptFriendRequestClick;
        this.onDenyFriendRequestClick = onDenyFriendRequestClick;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_list
                , parent, false);
        return new ViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest fr = requestsList.get(position);
        holder.bind(fr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return requestsList.size();
    }


    /**
     * Interface for accept friend request click
     */
    interface OnAcceptFriendRequestClick {
        void click(FriendRequest friendRequest);
    }

    /**
     * Interface for deny friend request click
     */
    interface OnDenyFriendRequestClick {
        void click(FriendRequest friendRequest);
    }

    /**
     * The ViewHolder for the adapter
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        Button acceptBtn;
        Button denyBtn;
        TextView userNameTxt;
        ImageView userProfileImage;

        /**
         * ViewHolder Constructor
         *
         * @param itemView the items view
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTxt = itemView.findViewById(R.id.request_user_name);
            acceptBtn = itemView.findViewById(R.id.accept_request_btn);
            denyBtn = itemView.findViewById(R.id.deny_request_btn);
            userProfileImage = itemView.findViewById(R.id.request_profile_image);
        }


        /**
         * binds the friend request to the view
         *
         * @param friendRequest a single friend request.
         */
        void bind(FriendRequest friendRequest) {
            acceptBtn.setOnClickListener(v -> onAcceptFriendRequestClick.click(friendRequest));
            denyBtn.setOnClickListener(v -> onDenyFriendRequestClick.click(friendRequest));

            DocumentReference otherUser =
                    FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION).document(friendRequest.getFriendFromId());
            otherUser.addSnapshotListener((value, error) -> {
                if (value != null) {
                    User user = value.toObject(User.class);
                    if (user != null) {
                        userNameTxt.setText(user.getFullName());
                        String imageUri = user.getProfilePhoto();
                        Glide.with(ctx).load(imageUri).placeholder(R.drawable.ic_baseline_person_24).into(userProfileImage);
                    }
                }
            });
        }
    }
}
