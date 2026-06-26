import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReadDb {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:scratch/vvv_db";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("Querying cart_items table:");
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM cart_items")) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.printf("Row %d: productId=%s, quantity=%d, productName=%s, price=%.2f, savedForLater=%b\n",
                            count,
                            rs.getString("productId"),
                            rs.getInt("quantity"),
                            rs.getString("productName"),
                            rs.getDouble("productPrice"),
                            rs.getBoolean("savedForLater")
                        );
                    }
                    if (count == 0) {
                        System.out.println("No items found in cart_items table.");
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
