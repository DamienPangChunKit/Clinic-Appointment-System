package com.example.clinicappointmentsystems;

public class AdminAppointmentHistory {
    String address, appointmentDate, appointmentTime, birthDate, email, gender, id, name, others, phone, status, symptoms, message, pushKey, price, report;

    public AdminAppointmentHistory() {

    }

    public AdminAppointmentHistory(String address, String appointmentDate, String appointmentTime, String birthDate, String email, String gender, String id, String name, String others, String phone, String status, String symptoms, String message, String pushKey, String price, String report) {
        this.address = address;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
        this.id = id;
        this.name = name;
        this.others = others;
        this.phone = phone;
        this.status = status;
        this.symptoms = symptoms;
        this.message = message;
        this.pushKey = pushKey;
        this.price = price;
        this.report = report;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getAddress() {
        return address;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOthers() {
        return others;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPushKey() {
        return pushKey;
    }

    public void setPushKey(String pushKey) {
        this.pushKey = pushKey;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }
}
