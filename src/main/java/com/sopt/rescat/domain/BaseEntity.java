package com.sopt.rescat.domain;

import javax.persistence.Column;
import javax.persistence.OneToOne;

public abstract class BaseEntity extends BaseTime {
    @OneToOne
    private User writer;

    @Column
    private Integer isConfirmed;
}