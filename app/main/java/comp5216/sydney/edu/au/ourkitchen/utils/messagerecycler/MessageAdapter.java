package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;

import static comp5216.sydney.edu.au.ourkitchen.utils.Constants.DATE_FORMAT_DDMMYYhhmm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

import comp5216.sydney.edu.au.ourkitchen.R;
import comp5216.sydney.edu.au.ourkitchen.model.Message;
import comp5216.sydney.edu.au.ourkitchen.model.User;
import comp5216.sydney.edu.au.ourkitchen.utils.Constants;

/**
 * Class for Custom Message Adapter for Recycler View
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messageList;
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private final FragmentActivity activity;
    private Context ctx;

    /**
     * Constructor
     *
     * @param messageList A List of Messages
     * @param activity    An instance of FragmentActivity
     */
    public MessageAdapter(List<Message> messageList, FragmentActivity activity) {
        this.messageList = messageList;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,
                        parent, false);
        ctx = parent.getContext();
        return new MessageAdapter.MessageViewHolder(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bindMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return messageList.size();
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
     * Function to clear message list
     */
    public void clear() {
        this.messageList.clear();
        notifyDataSetChanged();
    }

    /**
     * Function to add multiple messages in messagelist from an existing Message List
     *
     * @param list List of messages to add in messageList
     */
    public void addAll(List<Message> list) {
        this.messageList.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * A custom RecyclerView for Messages
     */
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView mMessage;
        private final TextView mNameAndTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mMessage = itemView.findViewById(R.id.message_item);
            mNameAndTime = itemView.findViewById(R.id.user_name_and_time);
        }

        /**
         * Function to bind message with class
         *
         * @param message The message to be bound
         */
        void bindMessage(Message message) {
            if (message != null) {
                mMessage.setText(message.getMessage());

                String uid = message.getSenderId();
                SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_DDMMYYhhmm);
                String strDate = formatter.format(message.getTimestamp().toDate());

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
