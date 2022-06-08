package comp5216.sydney.edu.au.ourkitchen.utils.friendsrecycler;

import comp5216.sydney.edu.au.ourkitchen.model.User;

/**
 * Operation class for the user recycler
 */
public class Operation {
    public User user;
    public int type;

    /**
     * Creates a new operation which takes in an integer type, so it can be a string resource as
     * well.
     *
     * @param user user
     * @param type type of operation, e.g. Modified, Added, Removed
     */
    public Operation(User user, int type) {
        this.user = user;
        this.type = type;
    }
}
