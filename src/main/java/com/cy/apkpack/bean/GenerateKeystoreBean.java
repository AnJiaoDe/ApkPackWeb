package com.cy.apkpack.bean;

public class GenerateKeystoreBean {

    private long validity;
    private String keyName;
    private String keyPwd;
    private String alias;
    private String aliasPwd;
    private String organization;
    private String city;
    private String province;

    public long getValidity() {
        return validity;
    }

    public void setValidity(long validity) {
        this.validity = validity;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyPwd() {
        return keyPwd;
    }

    public void setKeyPwd(String keyPwd) {
        this.keyPwd = keyPwd;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAliasPwd() {
        return aliasPwd;
    }

    public void setAliasPwd(String aliasPwd) {
        this.aliasPwd = aliasPwd;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
