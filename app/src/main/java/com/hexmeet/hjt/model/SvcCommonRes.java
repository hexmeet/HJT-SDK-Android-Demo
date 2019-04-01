package com.hexmeet.hjt.model;


public class SvcCommonRes {

    /**
     * result : ok
     * errorCode : 1007
     * errorInfo : Internal system error.
     * args : null
     */

    private String result;
    private String errorCode;
    private String errorInfo;
    private Object args;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public Object getArgs() {
        return args;
    }

    public void setArgs(Object args) {
        this.args = args;
    }
}
