package comp5216.sydney.edu.au.ourkitchen.utils.chatrecycler;

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

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * Adapter for list of chats, will display the chats as a scrollable list.
 */
public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.FriendViewHolder> {
    private final List<User> userList;
    private final FragmentActivity activity;
    private Context ctx;


    /**
     * Constructor
     *
     * @param userList A List of Users
     * @param activity An instance of FragmentActivity
     */
    public ChatsAdapter(List<User> userList, FragmentActivity activity) {
        this.userList = userList;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_card_recycler,
                        parent, false);
        ctx = parent.getContext();
        return new FriendViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = userList.get(position);
        holder.bindFriend(friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return userList.size();
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
     * Clears the adapter.
     */
    public void clear() {
        this.userList.clear();
        notifyDataSetChanged();
    }

    /**
     * Interface for onProfileImageClick
     */
    public interface OnProfileImageClick {
        void onProfileImageClick(String userId);
    }

    /**
     * Class to store friend list item in chats
     */
    class FriendViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImage;
        private final TextView userName;
        private final TextView friendInterests;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            profileImage = itemView.findViewById(R.id.profile_image);
            friendInterests = itemView.findViewById(R.id.friend_interests);
        }

        /**
         * Function to bind User with FriendView
         *
         * @param user An instance of User to be bound
         */
        void bindFriend(@NonNull User user) {
            setInfo(user);
        }

        /**
         * Function to set user info in friend view
         *
         * @param user An instance of User
         */
        private void setInfo(User user) {
            userName.setText(user.getFullName());
            if (user.getProfilePhoto() != null && !user.getProfilePhoto().equals("")) {
                Glide.with(ctx).load(user.getProfilePhoto()).into(profileImage);
            }

            OnProfileImageClick listener = userId -> {
                if (activity != null) {
                    NavController navController = Navigation.findNavController(activity,
                            R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.USER_UID, userId);
                    navController.navigate(R.id.message, bundle);
                }
            };
            profileImage.setOnClickListener(v -> listener.onProfileImageClick(user.getUid()));

            friendInterests.setText(user.getInterestsAsString());
        }
    }
}
