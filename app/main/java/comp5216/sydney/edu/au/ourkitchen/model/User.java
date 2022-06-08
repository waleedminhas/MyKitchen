package comp5216.sydney.edu.au.ourkitchen.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User class for custom Firestore object. Comments are all stored in the 'users' collection
 * on Firestore.
 */
public class User implements Serializable {
    public String uid;
    public String firstName;
    public String lastName;
    public String email;
    private List<String> posts;
    private List<String> likedPosts;
    private List<String> interests;
    private List<String> friends;
    private List<String> friendRequests;
    private String profilePhoto;
    private List<String> nameArrayCaseInsensitive;
    private boolean privateProfile;
    private String interestsAsString;
    private String fullName;


    /**
     * Empty constructor required for Firestore custom object.
     */
    public User() {
    }

    /**
     * User constructor.
     *
     * @param uid       user id
     * @param email     email
     * @param firstName first name
     * @param lastName  last name
     */
    public User(String uid, String email, String firstName, String lastName) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.posts = new ArrayList<>();
        this.likedPosts = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
        this.profilePhoto = "";
        this.nameArrayCaseInsensitive = new ArrayList<>();
        this.nameArrayCaseInsensitive.add(firstName.toLowerCase());
        this.nameArrayCaseInsensitive.add(lastName.toLowerCase());
        this.nameArrayCaseInsensitive.add(firstName.toLowerCase() + " " + lastName.toLowerCase());
        this.privateProfile = true;
    }

    /**
     * Constructor
     *
     * @param uid   user id
     * @param email email
     */
    public User(String uid, String email) {
        this.uid = uid;
        this.firstName = "";
        this.lastName = "";
        this.email = email;
        this.posts = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
        this.nameArrayCaseInsensitive = new ArrayList<>();
    }

    /**
     * Get profile photo.
     *
     * @return link to photo store in Firestore storage
     */
    public String getProfilePhoto() {
        return profilePhoto;
    }

    /**
     * Set profile photo.
     *
     * @param profilePhoto link to photo store in Firestore storage
     */
    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    /**
     * Get full name.
     *
     * @return first name and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * @param fullName the fullname of the user
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Get user id.
     *
     * @return user id
     */
    public String getUid() {
        return uid;
    }

    /**
     * Set user id.
     *
     * @param uid user id
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Get first name.
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set first name.
     *
     * @param firstName first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get last name.
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set last name.
     *
     * @param lastName last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get user's email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set user's email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get list of posts.
     *
     * @return list of post ids
     */
    public List<String> getPosts() {
        return posts;
    }

    /**
     * Set list of posts.
     *
     * @param posts list of post ids
     */
    public void setPosts(List<String> posts) {
        this.posts = posts;
    }

    /**
     * Add a post.
     *
     * @param post post object
     */
    public void addPost(Post post) {
        posts.add(post.getId());
    }

    /**
     * Get list of the user's liked posts.
     *
     * @return list of post ids
     */
    public List<String> getLikedPosts() {
        return likedPosts;
    }

    /**
     * Set list of liked posts.
     *
     * @param likedPosts a list of post ids
     */
    public void setLikedPosts(List<String> likedPosts) {
        this.likedPosts = likedPosts;
    }

    /**
     * Get list of user's interests.
     *
     * @return list of user's interests in lower case
     */
    public List<String> getInterests() {
        return interests;
    }

    /**
     * Set lists of interests as lower case versions.
     *
     * @param interests list of interests
     */
    public void setInterests(List<String> interests) {
        this.interests = interests.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    /**
     * Helper function to get lists as a string.
     *
     * @return string format of all interests
     */
    public String getInterestsAsString() {
        StringBuilder sb = new StringBuilder();
        if (interests != null && !interests.isEmpty()) {
            interests.forEach(interest -> {
                sb.append(interest);
                sb.append(", ");
            });
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("");
        }
        return sb.toString();


    }

    /**
     * a string of the users interests
     *
     * @param interestsAsString a string of the interests for display purposes
     */
    public void setInterestsAsString(String interestsAsString) {
        this.interestsAsString = interestsAsString;
    }

    /**
     * Add an interest to list.
     *
     * @param interest interest
     */
    public void addInterest(String interest) {
        if (!this.interests.contains(interest)) {
            this.interests.add(interest.toLowerCase());
        }
    }

    /**
     * Get user's friends.
     *
     * @return list of user ids
     */
    public List<String> getFriends() {
        return friends;
    }

    /**
     * Set user's friends.
     *
     * @param friends list of user ids
     */
    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    /**
     * Remove friend.
     *
     * @param friendId user id of friend to remove
     */
    public void removeFriend(String friendId) {
        this.friends.removeIf(f -> f.equals(friendId));
    }

    /**
     * Get list of friend requests.
     *
     * @return list of friendRequest ids
     */
    public List<String> getFriendRequests() {
        return friendRequests;
    }

    /**
     * Set list of friend requests.
     *
     * @param friendRequests list of friendRequest ids
     */
    public void setFriendRequests(List<String> friendRequests) {
        this.friendRequests = friendRequests;
    }

    /**
     * Get the name as a lower case version in an array. Helper for Firestore search query.
     *
     * @return list of name in lower case
     */
    public List<String> getNameArrayCaseInsensitive() {
        return nameArrayCaseInsensitive;
    }

    /**
     * Set the lower case name array.
     *
     * @param nameArrayCaseInsensitive list of name in lower case
     */
    public void setNameArrayCaseInsensitive(List<String> nameArrayCaseInsensitive) {
        this.nameArrayCaseInsensitive = nameArrayCaseInsensitive;
    }

    /**
     * Get whether the profile is public or not.
     *
     * @return true if profile is private, false if public
     */
    public boolean isPrivateProfile() {
        return privateProfile;
    }

    /**
     * Set whether the profile is public or private.
     *
     * @param privateProfile true for private, false for public
     */
    public void setPrivateProfile(boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    /**
     * To string method for user.
     *
     * @return string representation of user
     */
    @NonNull
    @Override
    public String toString() {
        return "firstName: " + getFirstName() + " lastName: " + lastName + " email: " + getEmail() + "interests: " + getInterestsAsString() + " profile_photo " + getProfilePhoto();
    }
}
