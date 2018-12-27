package com.sopt.rescat.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Region {
    @Column
    private Integer sdcode;

    @Column
    private String sdname;

    @Column
    private Integer sggcode;

    @Column
    private String sggname;

    @Column
    @Id
    private Integer emdcode;

    @Column
    private String emdname;
}
