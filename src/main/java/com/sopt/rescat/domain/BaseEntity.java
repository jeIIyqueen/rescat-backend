package com.sopt.rescat.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity extends BaseTime {
    @OneToOne
    private User writer;

    @Column
    // 0: 보류, 1: 승인, 2: 거절
    private Integer isConfirmed;
}