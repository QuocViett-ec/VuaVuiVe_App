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
    public void setTodayOrders(int todayOrders) { this.todayOrders = todayOrders; }
    public int getMonthOrders() { return monthOrders; }
    public void setMonthOrders(int monthOrders) { this.monthOrders = monthOrders; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
    public int getShippingCount() { return shippingCount; }
    public void setShippingCount(int shippingCount) { this.shippingCount = shippingCount; }
    public long getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(long totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
}
