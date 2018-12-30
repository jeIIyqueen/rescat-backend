package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.Breed;
import com.sopt.rescat.domain.enums.Vaccination;
import com.sopt.rescat.domain.photo.CarePostPhoto;
import com.sopt.rescat.dto.response.CarePostDto;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
public class CarePost extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    @Length(max = 500)
    private String contents;

    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL)
    private List<CarePostPhoto> photos;

    @OneToMany(mappedBy = "carePost", cascade = CascadeType.ALL)
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
    private int viewCount = 0;

    @Column
    private LocalDateTime startProtectionPeriod;

    @Column
    private LocalDateTime endProtectionPeriod;

    public CarePostDto toCarePostDto() {
        Integer MAIN_PHOTO_INDEX = 0;
        return CarePostDto.builder()
                .idx(idx)
                .name(name)
                .contents(contents)
                .viewCount(viewCount)
                .photo(photos.get(MAIN_PHOTO_INDEX))
                .createdAt(getCreatedAt())
                .build();
    }
}
