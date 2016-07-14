package victoralbertos.io.rxfcm.data.entities;

/**
 * Created by victor on 01/04/16.
 */
public class Notification {
    private final String title;
    private final String body;
    private final long timeStamp;

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
        this.timeStamp = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
