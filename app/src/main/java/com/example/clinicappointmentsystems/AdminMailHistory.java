package com.example.clinicappointmentsystems;

public class AdminMailHistory {
    public String name, email, phone, message, title, dateTime, status, mailID, pushKey;

    public AdminMailHistory() {

    }

    public AdminMailHistory(String name, String email, String phone, String title, String message, String dateTime, String status, String mailID, String pushKey) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.title = title;
        this.message = message;
        this.dateTime = dateTime;
        this.status = status;
        this.mailID = mailID;
        this.pushKey = pushKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateSent) {
        this.dateTime = dateSent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMailID() {
        return mailID;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public String getPushKey() {
        return pushKey;
    }

    public void setPushKey(String pushKey) {
        this.pushKey = pushKey;
    }
}
