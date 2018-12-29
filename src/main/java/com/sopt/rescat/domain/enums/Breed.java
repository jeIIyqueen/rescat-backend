package com.sopt.rescat.domain.enums;

public enum Breed {
    KOREANSHORTHAIR("코리안숏헤어"),
    PERSIAN("페르시안"),
    RUSSIANBLUE("러시안블루"),
    SIAM("샴"),
    TURKISHANGORA("터키쉬앙고라"),
    SCOTTISHFOLD("스코티쉬 폴드"),
    CHINCHILLA("친칠라"),
    ABYSSINIAN("아비시니안"),
    NORWEGIANFOREST("노르웨이숲"),
    BRITISHSHORTHAIR("브리티쉬숏헤어"),
    AMERICANSHORTHAIR("아메리칸숏헤어"),
    MUNCHKIN("먼치킨"),
    SCOTTISHSTRAIGHT("스코티쉬 스트레이트"),
    RAGDOLL("랙돌"),
    ETC("기타"),
    UNKNOWN("모름");

    private String korean;

    Breed(String korean) {
        this.korean = korean;
    }
}