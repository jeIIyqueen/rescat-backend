package com.sopt.rescat.domain;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Getter
@Builder
@Entity
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToMany(mappedBy = "notification")
    private List<User> receivingUser;

    @Column
    private String data;

    @Column
    private String contents;

    @Column
    // 0: 안읽음, 1: 읽음
    private Integer isChecked = 0;
}
