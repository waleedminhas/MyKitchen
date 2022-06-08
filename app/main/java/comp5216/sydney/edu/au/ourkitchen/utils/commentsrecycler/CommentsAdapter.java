package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.DATE_FORMAT_DDMMYYhhmm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Comment;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * Adapter to display list of comments.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private final List<Comment> commentList;
    private final FragmentActivity activity;
    private Context ctx;

    /**
     * Constructor.
     *
     * @param commentList list of comments
     * @param activity    current activity
     */
    public CommentsAdapter(List<Comment> commentList, FragmentActivity activity) {
        this.commentList = commentList;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CommentsAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType) {
        ctx = parent.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.comment_item,
                parent, false);
        return new CommentsAdapter.CommentViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bindComment(comment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return commentList.size();
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
     * Clears all the comments from the list
     */
    public void clear() {
        this.commentList.clear();
        notifyDataSetChanged();
    }

    /**
     * The view holder for each individual comment in the list.
     */
    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView mComment;
        TextView mNameAndTime;

        /**
         * Constructor
         *
         * @param itemView view to hold each individual comment
         */
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameAndTime = itemView.findViewById(R.id.user_name_and_time);
            mComment = itemView.findViewById(R.id.comment);
        }

        /**
         * Bind the comment to the view holder
         *
         * @param comment comment to display
         */
        void bindComment(Comment comment) {
            if (comment != null) {
                mComment.setText(comment.getCommentContent());

                String uid = comment.getUserId();
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_DDMMYYhhmm);
                String strDate = formatter.format(comment.getTimestamp().toDate());

                FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION).document(uid).addSnapshotListener((value, error) -> {
                    if (value != null) {
                        User user = value.toObject(User.class);
                        if (user != null) {
                            mNameAndTime.setText(user.getFullName() + ": " + strDate);
                        }
                    }
                });
            }
        }
    }
}
