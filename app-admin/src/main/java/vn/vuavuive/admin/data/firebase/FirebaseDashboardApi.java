package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.vuavuive.shared.data.api.DashboardApi;
import vn.vuavuive.shared.data.dto.ApiResponse;
import vn.vuavuive.shared.data.dto.DashboardStats;

public class FirebaseDashboardApi implements DashboardApi {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    @Override
    public Call<ApiResponse<DashboardStats>> getStats() {
        return new Call<ApiResponse<DashboardStats>>() {
            @Override public Response<ApiResponse<DashboardStats>> execute() { throw new UnsupportedOperationException(); }
            @Override
            public void enqueue(@NonNull Callback<ApiResponse<DashboardStats>> callback) {
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot rootSnapshot) {
                        DashboardStats stats = new DashboardStats();
                        
                        // 1. Calculate users count
                        int totalUsers = 0;
                        DataSnapshot usersSnap = rootSnapshot.child("users");
                        if (usersSnap.exists()) {
                            totalUsers = (int) usersSnap.getChildrenCount();
                        }
                        stats.setTotalUsers(totalUsers);

                        // 2. Process orders
                        int totalOrders = 0;
                        int todayOrders = 0;
                        int monthOrders = 0;
                        int pendingCount = 0;
                        int shippingCount = 0;
                        long totalRevenue = 0;

                        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
                        String monthStr = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());

                        DataSnapshot ordersSnap = rootSnapshot.child("orders");
                        if (ordersSnap.exists()) {
                            for (DataSnapshot oSnap : ordersSnap.getChildren()) {
                                totalOrders++;
                                
                                String status = oSnap.child("status").getValue(String.class);
                                if (status != null) {
                                    status = status.toLowerCase();
                                    if ("pending".equals(status)) pendingCount++;
                                    else if ("in_transit".equals(status) || "shipping".equals(status)) shippingCount++;
                                }

                                String createdAt = oSnap.child("created_at").getValue(String.class);
                                if (createdAt == null) createdAt = oSnap.child("createdAt").getValue(String.class);

                                if (createdAt != null) {
                                    if (createdAt.startsWith(todayStr)) todayOrders++;
                                    if (createdAt.startsWith(monthStr)) monthOrders++;
                                }

                                // Revenue calculation
                                String pStatus = oSnap.child("paymentStatus").getValue(String.class);
                                if (pStatus == null) pStatus = oSnap.child("payment_status").getValue(String.class);
                                if (pStatus == null) pStatus = oSnap.child("payment/status").getValue(String.class);
                                
                                if ("paid".equalsIgnoreCase(pStatus) || "delivered".equals(status)) {
                                    Double finalAmt = oSnap.child("final_amount").getValue(Double.class);
                                    if (finalAmt == null) finalAmt = oSnap.child("finalAmount").getValue(Double.class);
                                    if (finalAmt == null) finalAmt = oSnap.child("total_amount").getValue(Double.class);
                                    if (finalAmt == null) finalAmt = oSnap.child("totalAmount").getValue(Double.class);
                                    
                                    if (finalAmt != null) {
                                        totalRevenue += finalAmt.longValue();
                                    }
                                }
                            }
                        }

                        stats.setTotalOrders(totalOrders);
                        stats.setTodayOrders(todayOrders);
                        stats.setMonthOrders(monthOrders);
                        stats.setPendingCount(pendingCount);
                        stats.setShippingCount(shippingCount);
                        stats.setTotalRevenue(totalRevenue);

                        callback.onResponse(null, Response.success(ApiResponse.success(stats, "success", null, null)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResponse(null, Response.success(ApiResponse.error(error.getMessage())));
                    }
                });
            }
            @Override public boolean isExecuted() { return false; }
            @Override public void cancel() {}
            @Override public boolean isCanceled() { return false; }
            @NonNull @Override public Call<ApiResponse<DashboardStats>> clone() { return this; }
            @NonNull @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("https://firebase").build(); }
            @NonNull @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
        };
    }

    @Override
    public Call<ApiResponse<Map<String, Object>>> getAnalytics(String from, String to) {
        Map<String, Object> mockAnalytics = new HashMap<>();
        mockAnalytics.put("labels", new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"});
        mockAnalytics.put("revenue", new double[]{1200000, 1500000, 800000, 2200000, 1900000, 3100000, 2700000});
        mockAnalytics.put("orders", new int[]{12, 15, 8, 22, 19, 31, 27});
        return new FirebaseCall<>(ApiResponse.success(mockAnalytics, "success", null, null));
    }
}
