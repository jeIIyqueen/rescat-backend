package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;

@Entity
public class Photo extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    @Column
    @NonNull
    private String url;
}
