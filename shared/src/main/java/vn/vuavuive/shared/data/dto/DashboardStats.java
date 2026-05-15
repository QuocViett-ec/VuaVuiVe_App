package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class DashboardStats {
    @SerializedName("todayOrders")
    private int todayOrders;

    @SerializedName("monthOrders")
    private int monthOrders;

    @SerializedName("totalOrders")
    private int totalOrders;

    @SerializedName("pendingCount")
    private int pendingCount;

    @SerializedName("shippingCount")
    private int shippingCount;

    @SerializedName("totalRevenue")
    private long totalRevenue;

    @SerializedName("totalUsers")
    private int totalUsers;

    public int getTodayOrders() { return todayOrders; }
    public int getMonthOrders() { return monthOrders; }
    public int getTotalOrders() { return totalOrders; }
    public int getPendingCount() { return pendingCount; }
    public int getShippingCount() { return shippingCount; }
    public long getTotalRevenue() { return totalRevenue; }
    public int getTotalUsers() { return totalUsers; }
}
