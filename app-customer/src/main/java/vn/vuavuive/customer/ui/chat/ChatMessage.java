package vn.vuavuive.customer.ui.chat;

import java.util.List;
import java.util.UUID;

public class ChatMessage {
    private final String id;
    private final String content;
    private final boolean isUser;
    private final long timestamp;
    private final List<String> suggestions;   // quick-reply chips
    private final List<ProductItem> products; // sản phẩm từ AI

    /** Model sản phẩm trả về từ chatbot */
    public static class ProductItem {
        public String id;
        public String name;
        public double price;
        public String unit;
        public String imageUrl;

        public ProductItem() {}
        public ProductItem(String id, String name, double price, String unit, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.unit = unit;
            this.imageUrl = imageUrl;
        }
    }

    public ChatMessage(String content, boolean isUser) {
        this(content, isUser, null, null);
    }

    public ChatMessage(String content, boolean isUser, List<String> suggestions) {
        this(content, isUser, suggestions, null);
    }

    public ChatMessage(String content, boolean isUser, List<String> suggestions, List<ProductItem> products) {
        this.id          = UUID.randomUUID().toString();
        this.content     = content;
        this.isUser      = isUser;
        this.timestamp   = System.currentTimeMillis();
        this.suggestions = suggestions;
        this.products    = products;
    }

    public String getId()                    { return id; }
    public String getContent()               { return content; }
    public boolean isUser()                  { return isUser; }
    public long getTimestamp()               { return timestamp; }
    public List<String> getSuggestions()     { return suggestions; }
    public List<ProductItem> getProducts()   { return products; }
}
