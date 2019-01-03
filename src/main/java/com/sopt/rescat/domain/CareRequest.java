package com.sopt.rescat.domain;

import lombok.NonNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Entity
public class CareRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    @Range(min = 0, max = 1)
    // 0: 입양, 1: 임시보호
    private Integer type;

    @Column
    @Length(max = 10)
    @NonNull
    private String name;

    @Column
    @Length(max = 11)
    @NonNull
    private String phone;

    @Column
    @Past
    @NonNull
    private LocalDate birth;

    @Column
    @Length(max = 20)
    @NonNull
    private String job;

    @Column
    @Length(max = 50)
    @NonNull
    private String address;

    @Column
    @Range(min = 0, max = 3)
    @NonNull
    // 0: 아파트, 1: 주택, 2: 다세대주택, 3: 원룸
    private Integer houseType;

    @Column
    @NonNull
    private Boolean componionExperience;

    @Column
    private String finalWord;
}
