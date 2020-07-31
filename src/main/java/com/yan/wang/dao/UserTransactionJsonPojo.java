package com.yan.wang.dao;

public class UserTransactionJsonPojo {
    private String fee;
    private String order_id;
    private String datetime;
    private String usd;
    private String btc;
    private String btc_eur;
    private String type;
    private String id;
    private String eur;
    private String btc_usd;

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public String getBtc() {
        return btc;
    }

    public void setBtc(String btc) {
        this.btc = btc;
    }

    public String getBtc_eur() {
        return btc_eur;
    }

    public void setBtc_eur(String btc_eur) {
        this.btc_eur = btc_eur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEur() {
        return eur;
    }

    public void setEur(String eur) {
        this.eur = eur;
    }

    public String getBtc_usd() {
        return btc_usd;
    }

    public void setBtc_usd(String btc_usd) {
        this.btc_usd = btc_usd;
    }
}
