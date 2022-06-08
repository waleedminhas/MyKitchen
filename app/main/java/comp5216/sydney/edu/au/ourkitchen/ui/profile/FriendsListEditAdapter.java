package comp5216.sydney.edu.au.ourkitchen.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * FriendsListEditAdapter {@link RecyclerView.Adapter} is an adapter for a recyclerview for an
 * editable list of friends.
 */
public class FriendsListEditAdapter extends RecyclerView.Adapter<FriendsListEditAdapter.ViewHolder> {
    private static final String TAG = "FriendsListAdapter";
    private final List<User> friendsList;
    private final OnFriendClick onFriendClick;
    private final OnMoreClick onMoreClick;
    private Context ctx;

    /**
     * Constructor
     *
     * @param friendsList       a list of the users friends
     * @param listener          listener for when a friend is clicked
     * @param moreClickListener listener for when the more button is clicked
     */
    public FriendsListEditAdapter(List<User> friendsList, OnFriendClick listener,
                                  OnMoreClick moreClickListener) {
        this.friendsList = friendsList;
        this.onFriendClick = listener;
        this.onMoreClick = moreClickListener;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_style_2,
                        parent, false);
        ctx = parent.getContext();
        return new ViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
     * interface for the more button click
     */
    public interface OnMoreClick {
        void onMoreClick(String userId);
    }

    /**
     * interface for a friend click
     */
    public interface OnFriendClick {
        void onFriendClick(String userId);
    }

    /**
     * ViewHolder extends {@link RecyclerView.ViewHolder} as a holder for the friend view
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView friendImage;
        TextView friendName;
        ImageButton moreBtn;

        /**
         * The holder for the view
         *
         * @param itemView the view for the item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendImage = itemView.findViewById(R.id.friends_image);
            friendName = itemView.findViewById(R.id.friends_name);
            moreBtn = itemView.findViewById(R.id.more_options_button_friend_list);
        }

        /**
         * binds a user to the view holder
         *
         * @param user user to bind
         */
        void bindItem(@NonNull User user) {
            friendName.setText(user.getFullName());
            String imageURI = user.getProfilePhoto();
            Glide.with(ctx).load(imageURI).into(friendImage);
            friendImage.setOnClickListener(v -> onFriendClick.onFriendClick(user.getUid()));
            moreBtn.setOnClickListener(v -> onMoreClick.onMoreClick(user.getUid()));
        }
    }
}
