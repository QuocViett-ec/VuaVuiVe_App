package vn.vuavuive.shared.data.dto;

import com.google.gson.annotations.SerializedName;

public class DeliveryInfo {
    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("note")
    private String note;

    public DeliveryInfo() {}

    public DeliveryInfo(String name, String phone, String address, String note) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.note = note;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
