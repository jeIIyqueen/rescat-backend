package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;

@Entity
public class FundingComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 300)
    @NonNull
    private String contents;

    @Column
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_funding_idx"))
    private Funding funding;
}
