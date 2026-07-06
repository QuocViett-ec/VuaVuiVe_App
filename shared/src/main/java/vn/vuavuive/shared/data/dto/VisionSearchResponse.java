package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VisionSearchResponse {
    @SerializedName("keyword")
    private String keyword;

    @SerializedName("keywords")
    private List<String> keywords;

    @SerializedName("category")
    private String category;

    @SerializedName("confidence")
    private double confidence;

    public String getKeyword() { return keyword; }
    public List<String> getKeywords() { return keywords; }
    public String getCategory() { return category; }
    public double getConfidence() { return confidence; }
}
