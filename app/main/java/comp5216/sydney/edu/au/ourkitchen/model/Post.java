package comp5216.sydney.edu.au.ourkitchen.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Post class for custom Firestore object. Comments are all stored in the 'posts' collection
 * on Firestore.
 */
public class Post {
    private String userID;
    private String id;
    private float rating;
    private String recipeUrl;
    private String comment;
    private String modification;
    private String linkToPhoto;
    private Set<String> likedBy;
    private Timestamp timestamp;
    private Set<String> tags;
    private List<String> commentsIds;

    /**
     * Post constructor.
     *
     * @param userID       user id of post
     * @param id           id of post
     * @param rating       recipe rating
     * @param recipeUrl    url of review recipe
     * @param comment      comment on recipe
     * @param modification modification of recipe
     * @param linkToPhoto  link to user's uploaded photo
     * @param timestamp    timestamp of post
     * @param tags         list of tags
     */
    public Post(String userID, String id, float rating, String recipeUrl, String comment,
                String modification, String linkToPhoto, Timestamp timestamp, List<String> tags) {
        this.userID = userID;
        this.id = id;
        this.rating = rating;
        this.recipeUrl = recipeUrl;
        this.comment = comment;
        this.modification = modification;
        this.linkToPhoto = linkToPhoto;
        this.timestamp = timestamp;
        this.likedBy = new HashSet<>();
        this.tags = new HashSet<>(tags);
        this.commentsIds = new ArrayList<>();
    }

    /**
     * Empty constructor for Firestore custom object mapping.
     */
    public Post() {
    }

    /**
     * Get post comment.
     *
     * @return post comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set post comment.
     *
     * @param comment post comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Get recipe modification.
     *
     * @return recipe modification
     */
    public String getModification() {
        return modification;
    }

    /**
     * Set recipe modification.
     *
     * @param modification recipe modification
     */
    public void setModification(String modification) {
        this.modification = modification;
    }

    /**
     * Get recipe url.
     *
     * @return recipe url
     */
    public String getRecipeUrl() {
        return recipeUrl;
    }

    /**
     * Set recipe url.
     *
     * @param recipeUrl recipe url
     */
    public void setRecipeUrl(String recipeUrl) {
        this.recipeUrl = recipeUrl;
    }

    /**
     * Get link to user's uploaded photo.
     *
     * @return link to photo stored in Firestore storage
     */
    public String getLinkToPhoto() {
        return linkToPhoto;
    }

    /**
     * Set link to user's uploaded photo.
     *
     * @param linkToPhoto link to photo stored in Firestore storage
     */
    public void setLinkToPhoto(String linkToPhoto) {
        this.linkToPhoto = linkToPhoto;
    }

    /**
     * Get rating of recipe.
     *
     * @return rating between 0 and 5
     */
    public float getRating() {
        return rating;
    }

    /**
     * Set recipe rating.
     *
     * @param rating a rating between 0 and 5
     */
    public void setRating(float rating) {
        if (rating >= 0 && rating <= 5) {
            this.rating = rating;
        }
    }

    /**
     * Get the user id of who created the post.
     *
     * @return user id
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Set the user id of who crated the post.
     *
     * @param userID user id
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Get all of the users that liked the post.
     *
     * @return list of user ids
     */
    public List<String> getLikedBy() {
        return likedBy == null ? null : new ArrayList<>(likedBy);
    }

    /**
     * Set the users who liked the post.
     *
     * @param likedBy list of user ids
     */
    public void setLikedBy(List<String> likedBy) {
        this.likedBy = new HashSet<>(likedBy);
    }

    /**
     * Add a user to liked users list.
     *
     * @param userID user id
     */
    public void addLikedByUser(String userID) {
        if (userID != null) {
            if (this.likedBy == null) {
                this.likedBy = new HashSet<>();
            }
            this.likedBy.add(userID);
        }
    }

    /**
     * Remove a user from list of likedBy
     *
     * @param userID user id
     */
    public void removeLikedByUser(String userID) {
        if (userID != null) {
            this.likedBy.remove(userID);
        }
    }

    /**
     * Get the timestamp of post.
     *
     * @return post timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp of the post.
     *
     * @param timestamp post timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get id of the post.
     *
     * @return post id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the post.
     *
     * @param id post id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get tags of the post
     *
     * @return list of tags
     */
    public List<String> getTags() {
        if (tags == null) {
            return null;
        }
        return new ArrayList<>(tags);
    }

    /**
     * Set the tags for the post as lower case list of tags
     *
     * @param tags list of tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }


    /**
     * Get list of associated comments as ids.
     *
     * @return list of comment ids
     */
    public List<String> getCommentsIds() {
        return commentsIds;
    }

    /**
     * Set list of comment ids
     *
     * @param comments list of comment ids
     */
    public void setComments(List<String> comments) {
        this.commentsIds = comments;
    }

    /**
     * Compares post to specified object.
     *
     * @param o post object to compare to
     * @return true if equal, false if not
     */
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        return timestamp.equals(post.getTimestamp()) && userID.equals(post.getUserID()) && (recipeUrl == null ? post.getRecipeUrl() == null : recipeUrl.equals(post.getRecipeUrl())) && (comment == null ? post.getComment() == null : comment.equals(post.getComment())) && (modification == null ? post.getModification() == null : modification.equals(post.getModification()));
    }

    /**
     * To string method for post.
     *
     * @return string representation of post
     */
    @NonNull
    @Override
    public String toString() {
        return "Post{" + "recipeUrl='" + recipeUrl + '\'' + ", comment='" + comment + '\'' + ", " + "modification='" + modification + '\'' + '}';
    }
}
