/*
NAME: REDDAMMAGARI SREE SOUMYA
ID: 1001646494
NET-ID: sxr6494 */
import java.io.Serializable;

// A message that represents a request or a response
public class Message implements Serializable {

    private String message;
    private Object data;
    private Object extraData;

    // Send a request
    public Message(String request) {
        this(request, null);
    }

    // Send a request
    public Message(String request, Object data) {
        this(request, data, null);
    }

    // Send a request with additional data
    public Message(String request, Object data, Object extraData) {
        this.message = request;
        this.data = data;
        this.extraData = extraData;
    }

    // Get the request message
    public String getMessage() {
        return message;
    }

    // Get the request data
    public Object getData() {
        return data;
    }

    // Get any extra data
    public Object getExtraData() {
        return extraData;
    }
}
