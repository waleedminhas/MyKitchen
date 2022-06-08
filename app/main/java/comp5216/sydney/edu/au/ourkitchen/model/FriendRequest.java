package comp5216.sydney.edu.au.ourkitchen.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * FriendRequest class for custom Firestore object. FriendRequests are all stored in the
 * 'friendRequests' collection in Firestore.
 */
public class FriendRequest implements Serializable {
    private String status;
    private String friendFromId;
    private String friendToId;
    private String id;
    private Timestamp timestamp;

    /**
     * Empty constructor required for Firestore custom object.
     */
    public FriendRequest() {
    }

    /**
     * Constructor for friend request.
     *
     * @param friendFromId who sent the friend request
     * @param friendToId   who they sent the friend request to
     */
    public FriendRequest(String friendFromId, String friendToId) {
        this.friendFromId = friendFromId;
        this.friendToId = friendToId;
        this.status = FriendRequestStatus.PENDING.name();
        this.timestamp = new Timestamp(new Date());
    }

    /**
     * Get id of friend request.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id of request.
     *
     * @param id request id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get timestamp of request.
     *
     * @return timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp of request.
     *
     * @param timestamp request timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get status of request.
     *
     * @return friendship status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set status of request.
     *
     * @param status friendship status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get who sent the friend request.
     *
     * @return friend id, which id of a User in the 'users' Firestore collection
     */
    public String getFriendFromId() {
        return friendFromId;
    }

    /**
     * Set who sent the friend request.
     *
     * @param friendFromId friend id, which id of a User in the 'users' Firestore collection
     */
    public void setFriendFromId(String friendFromId) {
        this.friendFromId = friendFromId;
    }

    /**
     * Get who the friend request has been sent to.
     *
     * @return friend id, which id of a User in the 'users' Firestore collection
     */
    public String getFriendToId() {
        return friendToId;
    }

    /**
     * Set who the friend request has been sent to.
     *
     * @param friendToId friend id, which id of a User in the 'users' Firestore collection
     */
    public void setFriendToId(String friendToId) {
        this.friendToId = friendToId;
    }
}
