package com.mammapasta.dispatch;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class Order {
    public String orderId;
    public String customerName;
    public String deliveryAddress;
    public List<String> items;
    public double totalPrice;
    public String status;
    public long timestamp;
    public String customerEmail;
    public String phoneNumber;

    public Order() {

    }

    public Order(String orderId, String customerName, String deliveryAddress, List<String> items, double totalPrice) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    public Order(String orderId, String customerName, String deliveryAddress,
                 List<String> items, double totalPrice, String customerEmail, String phoneNumber) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.items = items;
        this.totalPrice = totalPrice;
        this.customerEmail = customerEmail;
        this.phoneNumber = phoneNumber;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}