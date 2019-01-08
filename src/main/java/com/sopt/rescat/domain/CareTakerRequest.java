package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.RequestStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CareTakerRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(hidden = true)
    private Long idx;

    @Column
    @NonNull
    // 0: 케어테이커 인증요청, 1: 지역 추가요청
    private Integer type;

    @Column
    @NonNull
    @Length(max = 10)
    @ApiModelProperty(notes = "인증하고자 하는 유저의 이름", position = 3)
    private String name;

    @Column
    @NonNull
    @Length(max = 11)
    @Pattern(regexp = "^01[0|1|6-9][0-9]{3,4}[0-9]{4}$", message = "잘못된 전화번호 형식입니다.")
    private String phone;

    @OneToOne
    @NonNull
    @ApiModelProperty(hidden = true)
    private Region region;

    @Column
    @NonNull
    @URL
    @NotNull
    @ApiModelProperty(notes = "인증 사진 url")
    private String authenticationPhotoUrl;

    @Column
    @ApiModelProperty(hidden = true)
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @Transient
    @ApiModelProperty(notes = "지역 전체 이름", required = true)
    private String regionFullName;

    @Transient
    @ApiModelProperty(notes = "요청자 이름")
    private String nickname;

    @Builder
    public CareTakerRequest(User writer, @NonNull @Length(max = 10) String name, @NonNull @Length(max = 11) @Pattern(regexp = "^01[0|1|6-9]-[0-9]{3,4}-[0-9]{4}$", message = "잘못된 전화번호 형식입니다.") String phone,
                            Region region,  @NonNull @URL @NotNull String authenticationPhotoUrl, @Range(min = 0, max = 2) Integer isConfirmed, Integer type) {
        super(writer);
        this.name = name;
        this.phone = phone;
        this.region = region;
        this.authenticationPhotoUrl = authenticationPhotoUrl;
        this.isConfirmed = isConfirmed;
        this.type = type;
    }

    public void fillUserNickname() {
        this.nickname = getWriter().getNickname();
    }

    public void approve() {
        this.isConfirmed = RequestStatus.CONFIRM.getValue();
    }

    public void refuse() {
        this.isConfirmed = RequestStatus.REFUSE.getValue();
    }
}