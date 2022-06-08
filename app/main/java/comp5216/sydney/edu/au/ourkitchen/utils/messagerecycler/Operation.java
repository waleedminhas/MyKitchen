package comp5216.sydney.edu.au.ourkitchen.utils.messagerecycler;

import comp5216.sydney.edu.au.ourkitchen.model.Message;

/**
 * Operation class for the message recycler view
 */
public class Operation {
    public Message message;
    public int type;

    /**
     * Creates a new operation which takes in an integer type, so it can be a string resource as
     * well.
     *
     * @param message chat message
     * @param type    type of operation, e.g. Modified, Added, Removed
     */
    public Operation(Message message, int type) {
        this.message = message;
        this.type = type;
    }
}
