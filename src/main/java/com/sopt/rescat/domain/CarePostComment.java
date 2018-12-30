package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;

import javax.persistence.*;

@Getter
@Entity
public class CarePostComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 300)
    @NonNull
    private String contents;

    @Column
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_care_post_idx"))
    @JsonIgnore
    private CarePost carePost;
}
