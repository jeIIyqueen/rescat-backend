package com.sopt.rescat.domain.enums;

public enum RequestType {
    CARETAKER(0),
    MAP(1),
    CAREPOST(2),
    FUNDING(3),
    CAREAPPLICATION(4),
    REGION(5),
    TEMPORALCAREPOST(6); //알림함 임시보호 체크


    private Integer value;

    RequestType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
