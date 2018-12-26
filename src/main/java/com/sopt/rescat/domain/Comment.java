package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;

@Entity
public class Comment<T> extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 300)
    @NonNull
    private String contents;

    @OneToOne
    private Photo photo;
}
