package com.sopt.rescat.domain;

import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Post extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne
    private User writer;

    @Column
    @NonNull
    @Length(max = 100)
    private String title;

    @Column
    @NonNull
    @Length(max = 500)
    private String contents;

    @OneToMany
    private List<Photo> photos;
}
