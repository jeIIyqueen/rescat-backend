package com.sopt.rescat.domain.enums;

public enum  MarkerType {
    // 0: 고양이, 1: 배식소, 2: 병원
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