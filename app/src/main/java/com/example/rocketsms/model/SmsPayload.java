package com.example.rocketsms.model;

public class SmsPayload {

    private final String phoneNumber;
    private final String message;

    public SmsPayload(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }
}
