package com.hexmeet.hjt.event;

public class UserInfoEvent {
    public String username;
    public String displayName;
    public String org;
    public String email;
    public String cellphone;
    public String telephone;
    public String dept;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public UserInfoEvent(String username, String displayName, String org, String email, String cellphone, String telephone, String dept) {
        this.username = username;
        this.displayName = displayName;
        this.org = org;
        this.email = email;
        this.cellphone = cellphone;
        this.telephone = telephone;
        this.dept = dept;
    }

    @Override
    public String toString() {
        return "UserInfoEvent{" +
                "username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", org='" + org + '\'' +
                ", email='" + email + '\'' +
                ", cellphone='" + cellphone + '\'' +
                ", telephone='" + telephone + '\'' +
                ", dept='" + dept + '\'' +
                '}';
    }
}
