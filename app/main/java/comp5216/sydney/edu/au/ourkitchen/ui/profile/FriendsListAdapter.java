package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * FriendsListAdapter extends {@link RecyclerView.Adapter} to create a recycler view for a list
 * of friends of the user.
 */
public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder> {
    private static final String TAG = "FriendsListAdapter";
    private final List<User> friendsList;
    private final OnFriendClick onFriendClick;
    private Context ctx;

    /**
     * Constructor
     *
     * @param friendsList the list of friends
     * @param listener    a listener for when the friend is clicked
     */
    public FriendsListAdapter(List<User> friendsList, OnFriendClick listener) {
        this.friendsList = friendsList;
        this.onFriendClick = listener;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_style_1,
                        parent, false);
        ctx = parent.getContext();
        return new FriendsViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        User user = friendsList.get(position);
        holder.bindItem(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    /**
     * Interface for the friend click
     */
    public interface OnFriendClick {
        void onFriendClick(String userId);
    }

    /**
     * FriendsViewHolder extends {@link RecyclerView.ViewHolder} to hold the view for each friend
     * in the list.
     */
    class FriendsViewHolder extends RecyclerView.ViewHolder {
        ImageView friendImage;
        TextView friendName;

        /**
         * Constructor for the view holder
         */
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            friendImage = itemView.findViewById(R.id.friends_image);
            friendName = itemView.findViewById(R.id.friends_name);
        }

        /**
         * Binds the user to the recycler view
         *
         * @param user the user for this view.
         */
        void bindItem(@NonNull User user) {
            friendName.setText(user.getFullName());
            String imageURI = user.getProfilePhoto();
            Glide.with(ctx).load(imageURI).placeholder(R.drawable.ic_baseline_person_24).into(friendImage);

            friendImage.setOnClickListener(v -> onFriendClick.onFriendClick(user.getUid()));
        }
    }
}
