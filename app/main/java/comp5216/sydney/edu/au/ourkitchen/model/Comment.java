package comp5216.sydney.edu.au.ourkitchen.model;

import com.google.firebase.Timestamp;

import java.util.Objects;

/**
 * Comment class for custom Firestore object. Comments are all stored in the 'comments' collection
 * on Firestore.
 */
public class Comment {
    private String id;
    private String postId;
    private String userId;
    private Timestamp timestamp;
    private String commentContent;

    /**
     * Constructor for comment .
     *
     * @param id             comment id
     * @param postId         post id
     * @param userId         user id
     * @param timestamp      timestamp
     * @param commentContent comment contents
     */
    public Comment(String id, String postId, String userId, Timestamp timestamp,
                   String commentContent) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.commentContent = commentContent;
    }

    /**
     * Empty constructor required for firestore.
     */
    public Comment() {
    }

    /**
     * Get the associated post id for the comment.
     *
     * @return post ic
     */
    public String getPostId() {
        return postId;
    }

    /**
     * Set the id of associated post.
     *
     * @param postId post id
     */
    public void setPostId(String postId) {
        this.postId = postId;
    }

    /**
     * Get user id associated with the comment
     *
     * @return user id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user id
     *
     * @param userId user id of who commented
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get timestamp.
     *
     * @return comment timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp.
     *
     * @param timestamp comment timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get comment content.
     *
     * @return comment content
     */
    public String getCommentContent() {
        return commentContent;
    }

    /**
     * Set comment content.
     *
     * @param commentContent comment content
     */
    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    /**
     * Get comment id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set comment id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Compares comment to specified object.
     *
     * @param o comment object to compare to
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return postId.equals(comment.postId) && userId.equals(comment.userId) && timestamp.equals(comment.timestamp) && commentContent.equals(comment.commentContent);
    }

    /**
     * Generates hashcode.
     *
     * @return int has code
     */
    @Override
    public int hashCode() {
        return Objects.hash(postId, userId, timestamp, commentContent);
    }
}