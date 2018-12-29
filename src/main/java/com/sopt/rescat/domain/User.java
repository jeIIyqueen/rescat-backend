package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.NotMatchException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class User extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

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
    @Length(max = 11)
    private String phone;

    @Column
    @NonNull
    @Length(max = 300)
    private String password;

    @OneToOne
    private Region mainRegion;

    @OneToOne
    private Region subRegion1;

    @OneToOne
    private Region subRegion2;

    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Role role;

    @Column
    private String photoUrl;

    @Builder
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.MEMBER;
    }

    public boolean matchPasswordBy(UserLoginDto userLoginDto, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(userLoginDto.getPassword(), this.password)) {
            throw new NotMatchException("password", "비밀번호가 일치하지 않습니다.");
        }
        return true;
    }
}
