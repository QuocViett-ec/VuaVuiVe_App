package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("limit")
    private int limit;

    @SerializedName("totalPages")
    private int totalPages;

    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public int getTotalPages() { return totalPages; }
}
