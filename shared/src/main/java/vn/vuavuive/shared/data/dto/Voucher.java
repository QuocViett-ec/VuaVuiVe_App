package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class Voucher {
    @SerializedName("_id")
    private String id;

    @SerializedName("code")
    private String code;  // unique, uppercase

    @SerializedName("type")
    private String type;  // "ship", "percent", "fixed"

    @SerializedName("value")
    private double value;

    @SerializedName("cap")
    private double cap;

    @SerializedName("minOrderValue")
    private double minOrderValue;

    @SerializedName("maxUses")
    private int maxUses;

    @SerializedName("usedCount")
    private int usedCount;

    @SerializedName("startsAt")
    private String startsAt;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("note")
    private String note;

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getType() { return type; }
    public double getValue() { return value; }
    public double getCap() { return cap; }
    public double getMinOrderValue() { return minOrderValue; }
    public int getMaxUses() { return maxUses; }
    public int getUsedCount() { return usedCount; }
    public String getStartsAt() { return startsAt; }
    public String getExpiresAt() { return expiresAt; }
    public boolean isActive() { return isActive; }
    public String getNote() { return note; }

    public boolean isShipVoucher() { return "ship".equals(type); }
    public boolean isPercentVoucher() { return "percent".equals(type); }
    public boolean isFixedVoucher() { return "fixed".equals(type); }

    // Setters for admin
    public void setCode(String code) { this.code = code; }
    public void setType(String type) { this.type = type; }
    public void setValue(double value) { this.value = value; }
    public void setCap(double cap) { this.cap = cap; }
    public void setMinOrderValue(double minOrderValue) { this.minOrderValue = minOrderValue; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }
    public void setStartsAt(String startsAt) { this.startsAt = startsAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(boolean active) { isActive = active; }
    public void setNote(String note) { this.note = note; }
}
