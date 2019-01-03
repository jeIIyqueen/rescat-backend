package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import com.sopt.rescat.dto.response.CarePostResponseDto;
import com.sopt.rescat.exception.InvalidValueException;
import com.sopt.rescat.exception.NotExistException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CarePost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String contents;

    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CarePostPhoto> photos;

    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CarePostComment> comments;

    @Column
    @NonNull
    @Length(max = 30)
    private String name;

    @Column
    @NonNull
    // 0: 입양, 1: 임시보호
    private Integer type;

    @Column
    @NonNull
    private String age;

    @Column
    @NonNull
    // 0: 남, 1: 여
    private Integer sex;

    @Column
    @Enumerated(EnumType.STRING)
    @NonNull
    private Breed breed;

    @Column
    @Enumerated(EnumType.STRING)
    private Vaccination vaccination;

    @Column
    // 0: 미완료, 1: 완료
    private Integer tnr;

    @Column
    private String etc;

    @Column
    @Builder.Default
    private int viewCount = 0;

    @Column
    private LocalDateTime startProtectionPeriod;

    @Column
    private LocalDateTime endProtectionPeriod;

    @Column
    @NonNull
    @Range(min = 0, max = 2)
    private Integer isConfirmed;

    @Column
    @NonNull
    private Boolean isFinished;

    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CareApplication> careApplications;

    @Transient
    private String nickname;

    @Transient
    private Boolean isSubmitted;

    @Transient
    private Boolean isWriter;

    public CarePost setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public CarePostResponseDto toCarePostDto() {
        Integer MAIN_PHOTO_INDEX = 0;
        if (photos.size() == MAIN_PHOTO_INDEX) throw new NotExistException("photo", "해당 글의 사진이 등록되어 있지 않습니다.");

        return CarePostResponseDto.builder()
                .idx(idx)
                .name(name)
                .viewCount(viewCount)
                .photo(photos.get(MAIN_PHOTO_INDEX))
                .createdAt(getCreatedAt())
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

    public CarePost setSubmitStatus(User loginUser) {
        this.isSubmitted = this.isSubmitted(loginUser);
        this.isWriter = this.equalsWriter(loginUser);
        return this;
    }

    public boolean equalsType(Integer type) {
        return this.type.equals(type);
    }

    public void updateUpdatedAt() {
        if (Duration.between(this.getUpdatedAt(), LocalDateTime.now()).getSeconds() < 259200) {
            throw new InvalidValueException("updatedAt", "끌올은 3일에 한번만 가능합니다.");
        }
        initUpdatedAt();
    }
}