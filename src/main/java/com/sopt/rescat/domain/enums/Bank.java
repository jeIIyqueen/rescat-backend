package com.sopt.rescat.domain.enums;

public enum Bank {
    KAKAOBANK("카카오뱅크"),
    NHNONGHYUP("NH농협"),
    SHINHAN("신한"),
    IBK("IBK기업"),
    KEBHANA("KEB하나"),
    WOORI("우리"),
    KBSTAR("KB국민"),
    STANDARDCHARTERED("SC제일"),
    KFCC("새마을"),
    KDB("KDB산업"),
    CITIBANK("씨티"),
    SUHYUP("수협"),
    EPOSTBANK("우체국");

    private String value;

    Bank(String value) {
        this.value = value;
    }
}
