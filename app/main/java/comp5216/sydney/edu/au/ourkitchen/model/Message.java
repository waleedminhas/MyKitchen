package comp5216.sydney.edu.au.ourkitchen.model;

import com.google.firebase.Timestamp;

import java.util.Date;

/**
 * This class stores the message, sender, receiver and timestamp of a chat message
 */
public class Message {
    private String id;
    private String senderId;
    private String recieverId;
    private String message;
    private Timestamp timestamp;

    /**
     * Constructor method to create an instance of Message class
     *
     * @param id         The id of the message
     * @param senderId   The id of the sender user
     * @param recieverId The id of the receiver user
     * @param timestamp  An instance of Timestmap
     * @param message    The message exchanged between the two users
     */
    public Message(String id, String senderId, String recieverId, Timestamp timestamp,
                   String message) {
        this.id = id;
        this.senderId = senderId;
        this.recieverId = recieverId;
        this.message = message;
        this.timestamp = new Timestamp(new Date());
    }

    /**
     * Constructor method for firestore
     */
    public Message() {
        // Leave empty for firestore
    }

    /**
     * Method to get the sender Id from Message object
     *
     * @return senderId The id of the sender user
     */
    public String getSenderId() {
        return senderId;
    }

    /**
     * Method to set the sender Id in Message object
     *
     * @param sender_id The id to be set as sender Id
     */
    public void setSenderId(String sender_id) {
        this.senderId = sender_id;
    }

    /**
     * Method to get the receiver Id from Message object
     *
     * @return receiverId The id of the receiver user
     */
    public String getRecieverId() {
        return recieverId;
    }

    /**
     * Method to set the receiver Id in Message object
     *
     * @param reciever_id The id to be set as receiver Id
     */
    public void setRecieverId(String reciever_id) {
        this.recieverId = reciever_id;
    }

    /**
     * Method to get the exchanged message between users from Message object
     *
     * @return message The message exchanged between sender and receiver
     */
    public String getMessage() {
        return message;
    }

    /**
     * Method to set the message in Message object
     *
     * @param message The message to be set in Message object
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Method to get the Id of the message in Message object
     *
     * @ return The id of the message
     */
    public String getId() {
        return id;
    }

    /**
     * Method to set the id of the message in Message object
     *
     * @param id The id to set as id in Message object
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Method to get Timestamp from Message object
     *
     * @return Timestamp instance
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Method to set the timestamp in Message object
     *
     * @param timestamp An instance of Timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
