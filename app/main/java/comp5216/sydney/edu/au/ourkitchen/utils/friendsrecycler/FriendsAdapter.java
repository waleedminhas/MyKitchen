package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * Adapter to display list of users.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private final List<User> friendList;
    private final FragmentActivity activity;
    private Context ctx;

    /**
     * Construct a new instance of the friends adapter
     *
     * @param friendList list of users
     * @param activity   current activity
     */
    public FriendsAdapter(List<User> friendList, FragmentActivity activity) {
        this.friendList = friendList;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card_recycler,
                        parent, false);
        ctx = parent.getContext();
        return new FriendViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bindFriend(friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return friendList.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     * Clears all the friends from the adapter
     */
    public void clear() {
        this.friendList.clear();
        notifyDataSetChanged();
    }

    /**
     * The view holder for each individual user in the list.
     */
    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;
        private final TextView friendName;
        private final TextView friendPrivacy;
        private final TextView friendStatus;
        private final TextView friendInterests;

        /**
         * Constructor
         *
         * @param itemView view to hold each individual user
         */
        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friend_name);
            profileImage = itemView.findViewById(R.id.profile_image);
            friendPrivacy = itemView.findViewById(R.id.friends_privacy);
            friendStatus = itemView.findViewById(R.id.friend_status);
            friendInterests = itemView.findViewById(R.id.friend_interests);
        }

        /**
         * Bind the user to the recycler view
         *
         * @param user user to display
         */
        void bindFriend(@NonNull User user) {
            setInfo(user);
        }

        /**
         * Helper function to populate the information about the friend
         *
         * @param user user to display
         */
        private void setInfo(User user) {
            friendName.setText(user.getFullName());
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().equals("")) {
                Glide.with(ctx).load(user.getProfilePhoto()).into(profileImage);
            }

            profileImage.setOnClickListener(view -> {
                if (activity != null) {
                    NavController navController = Navigation.findNavController(activity,
                            R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.USER_UID, user.getUid());
                    navController.navigate(R.id.showProfile, bundle);
                }
            });

            if (user.isPrivateProfile()) {
                friendPrivacy.setText(R.string.private_profile);
            } else {
                friendPrivacy.setText(R.string.public_profile);
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (user.getFriends() != null && currentUser != null && user.getFriends().contains(currentUser.getUid())) {
                friendStatus.setText(R.string.friends);
            } else {
                friendStatus.setText(R.string.not_friends);
            }

            friendInterests.setText(user.getInterestsAsString());
        }
    }
}
