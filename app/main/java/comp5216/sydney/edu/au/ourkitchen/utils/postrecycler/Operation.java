package comp5216.sydney.edu.au.ourkitchen.utils.postrecycler;

import comp5216.sydney.edu.au.ourkitchen.model.Post;

/**
 * Operation class for the post recycler view
 */
public class Operation {
    public Post post;
    public int type;

    /**
     * Creates a new operation which takes in an integer type, so it can be a string resource as
     * well.
     *
     * @param post post
     * @param type type of operation, e.g. Modified, Added, Removed
     */
    public Operation(Post post, int type) {
        this.post = post;
        this.type = type;
    }
}
