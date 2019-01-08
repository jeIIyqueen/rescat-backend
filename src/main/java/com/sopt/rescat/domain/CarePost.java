package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotExistException;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;

@Slf4j
@Getter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class CarePost extends BaseEntity {
    private static final Integer SECONDS_OF_3DAYS = 259200;
    private static final Integer MAIN_PHOTO_INDEX = 0;

    @ApiModelProperty(readOnly = true, notes = "글번호")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    @ApiModelProperty(notes = "글 내용", required = true)
    private String contents;

    @ApiModelProperty(notes = "사진 리스트", required = true)
    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarePostPhoto> photos;

    @ApiModelProperty(notes = "댓글 리스트", readOnly = true)
    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CarePostComment> comments;

    @ApiModelProperty(notes = "고양이 이름", required = true)
    @Column
    @NonNull
    @Length(max = 30)
    private String name;

    @ApiModelProperty(notes = "글 타입(0: 입양, 1: 임시보호)", required = true)
    @Column
    @NonNull
    // 0: 입양, 1: 임시보호
    private Integer type;

    @ApiModelProperty(notes = "나이", required = true)
    @Column
    @NonNull
    private String age;

    @ApiModelProperty(notes = "성별(0: 남, 1: 여)", required = true)
    @Column
    @NonNull
    // 0: 남, 1: 여
    private Integer sex;

    @ApiModelProperty(notes = "품종", required = true)
    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Breed breed;

    @ApiModelProperty(notes = "예방접종여부", required = true)
    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Vaccination vaccination;

    @ApiModelProperty(notes = "예방접종여부", required = true)
    @Column
    @NonNull
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @ApiModelProperty(notes = "추가적인 말")
    @Column
    private String etc;

    @ApiModelProperty(readOnly = true)
    @Column
    @Builder.Default
    private Integer viewCount = 0;

    @ApiModelProperty(notes = "임시보호 시작기간(글 타입 1일 경우 필수)")
    @Column
    private LocalDateTime startProtectionPeriod;

    @ApiModelProperty(notes = "임시보호 종료기간(글 타입 1일 경우 필수)")
    @Column
    private LocalDateTime endProtectionPeriod;

    @ApiModelProperty(notes = "관리자 승인 여부(0: 보류, 1: 승인, 2: 거절)")
    @Column
    @NonNull
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @ApiModelProperty(notes = "완료 여부")
    @Column
    @NonNull
    private Boolean isFinished;

    @ApiModelProperty(hidden = true)
    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CareApplication> careApplications;

    @ApiModelProperty(readOnly = true, notes = "작성/끌올 시간")
    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    @Column
    @Builder.Default
    private int warning = 0;

    @ApiModelProperty(readOnly = true, notes = "작성자 닉네임")
    @Transient
    private String nickname;

    @ApiModelProperty(readOnly = true, notes = "신청 여부")
    @Transient
    private Boolean isSubmitted;

    @ApiModelProperty(readOnly = true, notes = "작성자 일치 여부")
    @Transient
    private Boolean isWriter;

    public CarePost setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public CarePostResponseDto toCarePostDto() {
        if (photos.size() == MAIN_PHOTO_INDEX) throw new NotExistException("photo", "해당 글의 사진이 등록되어 있지 않습니다.");

        return CarePostResponseDto.builder()
                .idx(idx)
                .name(name)
                .type(type)
                .contents(contents)
                .viewCount(viewCount)
                .photo(photos.get(MAIN_PHOTO_INDEX))
                .createdAt(getCreatedAt())
                .updatedAt(updatedAt)
                .isFinished(isFinished)
                .build();
    }

    public CarePost initPhotos(List<CarePostPhoto> carePostPhotos) {
        this.photos = carePostPhotos;
        return this;
    }

    public CarePost setWriter(User writer) {
        initWriter(writer);
        return this;
    }

    public void updateConfirmStatus(Integer isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public CarePost addViewCount() {
        ++this.viewCount;
        return this;
    }

    @JsonIgnore
    public void finish() {
        this.isFinished = true;
    }

    public boolean isSubmitted(User loginUser) {
        return careApplications.stream().anyMatch(careApplication -> careApplication.isMyApplication(loginUser));
    }

    public boolean equalsWriter(User loginUser) {
        return this.getWriter().equals(loginUser);
    }

    public CarePost setStatus(User loginUser) {
        this.isSubmitted = this.isSubmitted(loginUser);
        this.isWriter = this.equalsWriter(loginUser);
        return this;
    }

    public boolean equalsType(Integer type) {
        return this.type.equals(type);
    }

    public void updateUpdatedAt() {
        if (Duration.between(this.getUpdatedAt(), LocalDateTime.now()).getSeconds() < SECONDS_OF_3DAYS)
            throw new InvalidValueException("updatedAt", "끌올은 3일에 한번만 가능합니다.");

        this.updatedAt = now();
    }

    public void warningCount() {
        ++this.warning;
    }

}