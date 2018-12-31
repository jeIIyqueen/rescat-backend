package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotMatchException;
import lombok.*;
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
    @JsonIgnore
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

    @Column
    private Long mileage;

    @Builder
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.MEMBER;
    }

    @Builder
    public User(String nickname){
        this.nickname = nickname;
    }

    public boolean matchPasswordBy(UserLoginDto userLoginDto, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(userLoginDto.getPassword(), this.password)) {
            throw new NotMatchException("password", "비밀번호가 일치하지 않습니다.");
        }
        return true;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    private void checkMileageMoreThan(Long mileage) {
        if(this.mileage < mileage) throw new InvalidValueException("mileage", "사용자가 가진 마일리지보다 더 큽니다.");
    }

    public void updateMileage(Long mileage) {
        checkMileageMoreThan(mileage);
        this.mileage += mileage;
    }
}
