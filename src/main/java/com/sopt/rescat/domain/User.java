package com.sopt.rescat.domain;

import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.NotMatchException;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
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
    @NonNull
    private String mainRegion;

    @Column
    private String subRegion1;

    @Column
    private String subRegion2;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_role_idx"))
    private Role role;

    @OneToOne
    private Photo photo;

    public boolean matchPasswordBy(UserLoginDto userLoginDto, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(userLoginDto.getPassword(), this.password)) {
            throw new NotMatchException("패스워드가 일치하지 않습니다.");
        }
        return true;
    }
}
