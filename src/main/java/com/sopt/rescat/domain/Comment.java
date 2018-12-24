package com.sopt.rescat.domain;

import lombok.NonNull;

import javax.persistence.*;

@Entity
public class Comment extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_post_idx"))
    private Post post;

    @OneToOne
    private User writer;

    @Column(length = 300)
    @NonNull
    private String contents;
}
