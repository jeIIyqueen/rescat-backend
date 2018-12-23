package org.sopt.appjam.rescat.domain;

import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    @Column
    @NonNull
    @Length(max = 10)
    private String name;

    @Column(unique = true)
    @NonNull
    @Length(max = 30)
    private String nickname;

    @Column(unique = true)
    @NonNull
    @Length(max = 50)
    private String id;

    @Column
    @NonNull
    @Length(max = 300)
    private String password;

    @Column
    private String regions;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_role_idx"))
    private Role role;

    @OneToOne
    private Photo photo;
}
