package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CarePost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    @Length(max = 500)
    private String contents;

    @OneToMany
    private List<Photo> photos;

    @OneToMany
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_post_comment_idx"))
    private List<Comment> comments;

    @Column
    @NonNull
    @Length(max = 30)
    private String name;

    @Column
    @NonNull
    private LocalDateTime birth;

    @Column
    @NonNull
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    @NonNull
    private Breed breed;

    @Column
    private Vaccination vaccination;

    @Column
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Column
    private String etc;

    @Column
    private LocalDateTime startProtectionPeriod;

    @Column
    private LocalDateTime endProtectionPeriod;
}
