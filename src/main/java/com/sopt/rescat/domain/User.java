package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Role;
import com.sopt.rescat.dto.JwtTokenDto;
import com.sopt.rescat.dto.RegionDto;
import com.sopt.rescat.dto.UserLoginDto;
import com.sopt.rescat.dto.response.UserLoginResponseDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotMatchException;
import com.sopt.rescat.exception.UnAuthenticationException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


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
    @Pattern(regexp = "^01[0|1|6-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "잘못된 전화번호 형식입니다.")
    private String phone;

    @Column
    @NonNull
    @Length(max = 300)
    @JsonIgnore
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_main_region_idx"))
    private Region mainRegion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_sub_1_region_idx"))
    private Region subRegion1;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_user_sub_2_region_idx"))
    private Region subRegion2;

    @Enumerated(value = EnumType.STRING)
    @Column
    @NonNull
    private Role role;

    @Column
    private Long mileage;

    @Column
    private String InstanceToken;


    @Transient
    @ApiModelProperty(notes = "지역 전체 이름", required = true)
    private String regionFullName;

    @Builder
    public User(String id, String password, String nickname) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.MEMBER;
        this.mileage = 0L;
    }

    public boolean match(User target) {
        return this.idx.equals(target.getIdx());
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
        } catch (Exception e) {
            throw new UnAuthenticationException("emdCode", "인가되지 않은 지역입니다.");
        }
        throw new UnAuthenticationException("emdCode", "인가되지 않은 지역입니다.");
    }

    public void grantCareTakerAuth(String phone, String name, Region mainRegion) {
        this.role = Role.CARETAKER;
        this.phone = phone;
        this.name = name;
        this.mainRegion = mainRegion;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }


    public void updateInstanceToken(String InstanceToken) {
        this.InstanceToken = InstanceToken;
    }


    public void deleteMainRegion(Region subRegion1, Region subRegion2) {
        this.mainRegion = subRegion1;
        this.subRegion1 = subRegion2;
        this.subRegion2 = null;
    }

    public void deleteSubRegion1(Region subRegion2) {
        this.subRegion1 = subRegion2;
        this.subRegion2 = null;
    }

    public void deleteSubRegion2() {
        this.subRegion2 = null;
    }

    public void updateRegions(Region mainRegion, Region subRegion1, Region subRegion2) {
        this.mainRegion = mainRegion;
        this.subRegion1 = subRegion1;
        this.subRegion2 = subRegion2;
    }

    public void addMainRegion(Region mainRegion) {
        this.mainRegion = mainRegion;
    }

    public void addSubRegion1(Region subRegion1) {
        this.subRegion1 = subRegion1;
    }

    public void addSubRegion2(Region subRegion2) {
        this.subRegion2 = subRegion2;
    }

    public List<RegionDto> getMyRegionDtoList() {
        List<RegionDto> regionDtos = new ArrayList<>();
        if (mainRegion != null)
            regionDtos.add(mainRegion.toRegionDto());
        if (subRegion1 != null)
            regionDtos.add(subRegion1.toRegionDto());
        if (subRegion2 != null)
            regionDtos.add(subRegion2.toRegionDto());
        return regionDtos;

    }

    public UserLoginResponseDto toUserLoginResponseDto(JwtTokenDto tokenDto) {
        return UserLoginResponseDto.builder()
                .idx(idx)
                .mileage(mileage)
                .regions(getMyRegionDtoList().stream().filter(Objects::nonNull).map(RegionDto::getName).collect(Collectors.toList()))
                .emdCodes(getMyRegionDtoList().stream().filter(Objects::nonNull).map(regionDto -> regionDto.getCode()).collect(Collectors.toList()))
                .role(role)
                .jwtTokenDto(tokenDto)
                .build();
    }
}
