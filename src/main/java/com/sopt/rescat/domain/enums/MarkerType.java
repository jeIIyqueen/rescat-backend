package com.sopt.rescat.domain.enums;

public enum  MarkerType {
    // 0: 배식소, 1: 병원, 2: 고양이
    CAFETERIA(0),
    HOSPITAL(1),
    Cat(2);

    private Integer value;

    MarkerType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}