package jellyqueen.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jellyqueen.rescat.domain.enums.HouseType;
import jellyqueen.rescat.exception.NotMatchException;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Slf4j
public class CareApplication extends BaseEntity {

    @ApiModelProperty(readOnly = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_care_application_care_post_idx"))
    @JsonIgnore
    private CarePost carePost;

    @Column
    @NonNull
    @NotNull
    @Range(min = 0, max = 1)
    @ApiModelProperty(notes = "신청하고자 하는 글의 유형(0: 입양, 1: 임시보호)", position = 1, required = true)
    // 0: 입양, 1: 임시보호
    private Integer type;

    @Column
    @Length(max = 10)
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "신청자 이름", position = 2, required = true)
    private String name;

    @Column
    @Pattern(regexp = "^(01[016789]{1}|02|0[3-9]{1}[0-9]{1})([0-9]{3,4})([0-9]{4})$", message = "잘못된 전화번호 형식입니다.")
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "신청자 연락처", position = 3, required = true)
    private String phone;

    @Column
    @Past
    @ApiModelProperty(notes = "신청자 생년월일", position = 4)
    private LocalDate birth;

    @Column
    @Length(max = 20)
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "신청자 직업", position = 5, required = true)
    private String job;

    @Column
    @Length(max = 50)
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "신청자 자택 주소", position = 6, required = true)
    private String address;

    @Enumerated(EnumType.ORDINAL)
    @Column
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "신청자 자택 형태(0: 아파트, 1: 주택, 2: 다세대주택, 3: 원룸)", position = 7, required = true)
    private HouseType houseType;

    @Column
    @NonNull
    @NotNull
    @ApiModelProperty(notes = "반려경험(true/false)", position = 8, required = true)
    private Boolean companionExperience;

    @Column
    @ApiModelProperty(notes = "추가적으로 하고싶은 말", position = 9)
    private String finalWord;

    @ApiModelProperty(readOnly = true)
    @Column
    private Boolean isAccepted;

    @ApiModelProperty(readOnly = true)
    @Transient
    private String title;

    @Builder
    public CareApplication(User writer, CarePost carePost, @NonNull @Range(min = 0, max = 1) Integer type, @Length(max = 10) @NonNull String name, @Length(max = 13) @NonNull String phone, @Past LocalDate birth, @Length(max = 20) @NonNull String job, @Length(max = 50) @NonNull String address, @NonNull HouseType houseType, @NonNull Boolean companionExperience, String finalWord, Boolean isAccepted) {
        super(writer);
        this.carePost = carePost;
        this.type = type;
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.job = job;
        this.address = address;
        this.houseType = houseType;
        this.companionExperience = companionExperience;
        this.finalWord = finalWord;
        this.isAccepted = isAccepted;
    }

    public void accept(User loginUser) {
        if (!loginUser.equals(this.getCarePost().getWriter()))
            throw new NotMatchException("user", "타인의 글에 대한 신청을 승낙할 수 없습니다.");
        this.isAccepted = true;
    }

    public boolean isMyApplication(User loginUser) {
        return this.getWriter().equals(loginUser);
    }
}
