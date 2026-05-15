package vn.vuavuive.customer.ui.chat;

import java.util.UUID;

public class ChatMessage {
    private final String id;
    private final String content;
    private final boolean isUser;
    private final long timestamp;

    public ChatMessage(String content, boolean isUser) {
        this.id        = UUID.randomUUID().toString();
        this.content   = content;
        this.isUser    = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId()        { return id; }
    public String getContent()   { return content; }
    public boolean isUser()      { return isUser; }
    public long getTimestamp()   { return timestamp; }
}
