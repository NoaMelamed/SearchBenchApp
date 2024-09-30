package com.example.noaandroid;

public class User {
    private String userName;
    private String userPwd;
    private String userEmail;
    private String userPhone;

    public User(String userEmail, String userName, String userPhone, String userPwd) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userPwd = userPwd;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
