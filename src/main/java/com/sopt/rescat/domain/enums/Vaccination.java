package com.sopt.rescat.domain.enums;

public enum  Vaccination {
    UNKNOWINGNESS("모름"),
    NOTHING("안함"),
    FIRST("1차"),
    SECOND("1차"),
    THIRD("1차");

    private String value;

    Vaccination(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
