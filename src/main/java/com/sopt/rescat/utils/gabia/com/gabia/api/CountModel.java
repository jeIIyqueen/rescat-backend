package com.sopt.rescat.utils.gabia.com.gabia.api;

public class CountModel {
    private String sms_quantity = "";
    private String ars_quantity = "";

    public CountModel(String sms_quantity, String ars_quantity) {
        this.setSms_quantity(sms_quantity);
        this.setArs_quantity(ars_quantity);
    }

    public String getSms_quantity() {
        return sms_quantity;
    }

    public void setSms_quantity(String sms_quantity) {
        this.sms_quantity = sms_quantity;
    }

    public String getArs_quantity() {
        return ars_quantity;
    }

    public void setArs_quantity(String ars_quantity) {
        this.ars_quantity = ars_quantity;
    }
}

