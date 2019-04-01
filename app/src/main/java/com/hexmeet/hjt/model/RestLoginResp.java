package com.hexmeet.hjt.model;

public class RestLoginResp {
    /**
     * token : 1110805475
     * username : 1197
     * displayName : RoneyAndroid118
     * org : 中创视讯
     * email : zhengcx@hexmeet.com
     * cellphone : 13838112081
     * telephone :
     * dept:
     * everChangedPasswd : false
     * customizedH5UrlPrefix:
     */
    public String username;
    public String displayName;
    public String org;
    public String email;
    public String cellphone;
    public String telephone;
    public String dept;
    public boolean everChangedPasswd;
    public String customizedH5UrlPrefix;
    public String token;
    public String doradoVersion;

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOrg() {
        return org;
    }

    public String getEmail() {
        return email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getDept() {
        return dept;
    }

    public boolean isEverChangedPasswd() {
        return everChangedPasswd;
    }

    public String getCustomizedH5UrlPrefix() {
        return customizedH5UrlPrefix;
    }

    public String getToken() {
        return token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setEverChangedPasswd(boolean everChangedPasswd) {
        this.everChangedPasswd = everChangedPasswd;
    }

    public void setCustomizedH5UrlPrefix(String customizedH5UrlPrefix) {
        this.customizedH5UrlPrefix = customizedH5UrlPrefix;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDoradoVersion() {
        return doradoVersion;
    }

    public void setDoradoVersion(String doradoVersion) {
        this.doradoVersion = doradoVersion;
    }

    public RestLoginResp() {}

    public RestLoginResp(String username, String displayName, String org, String email, String cellphone, String telephone, String dept, boolean everChangedPasswd, String customizedH5UrlPrefix, String token, String doradoVersion) {
        this.username = username;
        this.displayName = displayName;
        this.org = org;
        this.email = email;
        this.cellphone = cellphone;
        this.telephone = telephone;
        this.dept = dept;
        this.everChangedPasswd = everChangedPasswd;
        this.customizedH5UrlPrefix = customizedH5UrlPrefix;
        this.token = token;
        this.doradoVersion = doradoVersion;
    }
}
