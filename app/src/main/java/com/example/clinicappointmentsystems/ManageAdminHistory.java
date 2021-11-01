package com.example.clinicappointmentsystems;

public class ManageAdminHistory {
    String address, balance, birthDate, email, gender, image, password, phone, username, userType, role;

    public ManageAdminHistory() {

    }

    public ManageAdminHistory(String address, String balance, String birthDate, String email, String gender, String image, String password, String phone, String username, String userType, String role) {
        this.address = address;
        this.balance = balance;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
        this.image = image;
        this.password = password;
        this.phone = phone;
        this.username = username;
        this.userType = userType;
        this.role = role;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
