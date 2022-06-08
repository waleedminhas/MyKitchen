package comp5216.sydney.edu.au.ourkitchen.utils.commentsrecycler;

import comp5216.sydney.edu.au.ourkitchen.model.Comment;

/**
 * Operation class for the comment recycler
 */
public class Operation {
    public Comment comment;
    public int type;

    /**
     * Creates a new operation which takes in an integer type, so it can be a string resource as
     * well.
     *
     * @param comment comment
     * @param type    type of operation, e.g. Modified, Added, Removed
     */
    public Operation(Comment comment, int type) {
        this.comment = comment;
        this.type = type;
    }
}
