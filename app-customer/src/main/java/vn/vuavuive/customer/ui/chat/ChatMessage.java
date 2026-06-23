package vn.vuavuive.customer.ui.chat;

import java.util.List;
import java.util.UUID;

public class ChatMessage {
    private final String id;
    private final String content;
    private final boolean isUser;
    private final long timestamp;
    private final List<String> suggestions; // gợi ý quick reply (chỉ cho bot)

    public ChatMessage(String content, boolean isUser) {
        this(content, isUser, null);
    }

    public ChatMessage(String content, boolean isUser, List<String> suggestions) {
        this.id          = UUID.randomUUID().toString();
        this.content     = content;
        this.isUser      = isUser;
        this.timestamp   = System.currentTimeMillis();
        this.suggestions = suggestions;
    }

    public String getId()              { return id; }
    public String getContent()         { return content; }
    public boolean isUser()            { return isUser; }
    public long getTimestamp()         { return timestamp; }
    public List<String> getSuggestions() { return suggestions; }
}
