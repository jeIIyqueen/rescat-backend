package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.List;


@Getter
@Entity
@NoArgsConstructor
@Slf4j
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
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_main_region_idx"))
    private Region mainRegion;

    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_sub_1_region_idx"))
    private Region subRegion1;

    @OneToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_sub_2_region_idx"))
    private Region subRegion2;

    @Enumerated(value = EnumType.STRING)
    @Column
    @NonNull
    private Role role;

    @Column
    private String photoUrl;

    @Column
    private Long mileage;

    @Column
    private String deviceToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Notification", joinColumns = @JoinColumn(name = "user_idx"), inverseJoinColumns = @JoinColumn(name = "notification_idx"))
    private List<Notification> notification;

    @Builder
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.MEMBER;
        this.mileage = 0L;
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
        if (this.mileage + mileage < 0) throw new InvalidValueException("mileage", "사용자가 가진 마일리지는 음수가 될 수 없습니다.");
    }

    public void updateMileage(Long mileage) {
        checkMileageMoreThan(mileage);
        this.mileage += mileage;
    }

    public boolean isAuthenticatedRegion(Integer emdCode) {
        try {
            if (this.mainRegion.getEmdCode().equals(emdCode)
                    || this.subRegion1.getEmdCode().equals(emdCode)
                    || this.subRegion2.getEmdCode().equals(emdCode))
                return true;
        } catch (NullPointerException e) {
            throw new UnAuthenticationException("emdCode", "인가되지 않은 지역입니다.");
        }
        return false;
    }

    public void grantCareTakerAuth(String phone, String name, Region mainRegion) {
        this.role = Role.CARETAKER;
        this.phone = phone;
        this.name = name;
        this.mainRegion = mainRegion;
    }
    public void updateUser(String nickname, String phone){
        this.nickname = nickname;
        this.phone = phone;
    }

    public void updateDeviceToken(String deviceToken) {
        this.deviceToken=deviceToken;
    }
}
