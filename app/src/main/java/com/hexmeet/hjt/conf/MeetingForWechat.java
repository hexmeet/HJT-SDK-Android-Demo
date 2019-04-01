package com.hexmeet.hjt.conf;

import android.text.TextUtils;

public class MeetingForWechat {


    /**
     * name : 中创专用
     * numericId : 13500135000
     * password : 112233
     * domain : null
     * softEndpointJoinUrl :
     * hardEndpointJoinUrl : 13500135000*112233
     * h323EndpointJoinUrl : 172.24.0.68##13500135000##112233
     * sipEndpointJoinUrl : 13500135000*112233@172.24.0.68
     * url : http://172.16.0.22:8000/#/shareconf?numericId=13500135000
     */

    private String name;
    private String numericId;
    private String password;
    private Object domain;
    private String softEndpointJoinUrl;
    private String hardEndpointJoinUrl;
    private String h323EndpointJoinUrl;
    private String sipEndpointJoinUrl;
    private String url;
    /**
     * confName : SmallC的会议
     * confTime : 2018-07-23 13:53 - 2018-07-26 13:53
     * confPassword :
     * confId : 24604
     * remark :
     * domain : null
     */

    private String confName;
    private String confTime;
    private String confPassword;
    private int confId;
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumericId() {
        return numericId;
    }

    public void setNumericId(String numericId) {
        this.numericId = numericId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Object getDomain() {
        return domain;
    }

    public void setDomain(Object domain) {
        this.domain = domain;
    }

    public String getSoftEndpointJoinUrl() {
        return softEndpointJoinUrl;
    }

    public void setSoftEndpointJoinUrl(String softEndpointJoinUrl) {
        this.softEndpointJoinUrl = softEndpointJoinUrl;
    }

    public String getHardEndpointJoinUrl() {
        return hardEndpointJoinUrl;
    }

    public void setHardEndpointJoinUrl(String hardEndpointJoinUrl) {
        this.hardEndpointJoinUrl = hardEndpointJoinUrl;
    }

    public String getH323EndpointJoinUrl() {
        return h323EndpointJoinUrl;
    }

    public void setH323EndpointJoinUrl(String h323EndpointJoinUrl) {
        this.h323EndpointJoinUrl = h323EndpointJoinUrl;
    }

    public String getSipEndpointJoinUrl() {
        return sipEndpointJoinUrl;
    }

    public void setSipEndpointJoinUrl(String sipEndpointJoinUrl) {
        this.sipEndpointJoinUrl = sipEndpointJoinUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public String getConfTime() {
        return confTime;
    }

    public void setConfTime(String confTime) {
        this.confTime = confTime;
    }

    public String getConfPassword() {
        return confPassword;
    }

    public void setConfPassword(String confPassword) {
        this.confPassword = confPassword;
    }

    public int getConfId() {
        return confId;
    }

    public void setConfId(int confId) {
        this.confId = confId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getMeetingName() {
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        return confName;
    }

    public String getMeetingPassword() {
        if (!TextUtils.isEmpty(password)) {
            return name;
        }
        return confPassword;
    }
}
