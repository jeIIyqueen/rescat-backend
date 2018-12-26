package com.sopt.rescat.domain;

import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@Entity
public class CareTakerRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne
    @NonNull
    private User user;

    @Column
    @NonNull
    @Length(max = 11)
    private String phone;

    @Column
    @NonNull
    private String mainRegion;

    @Column
    private String subRegion1;

    @Column
    private String subRegion2;

    @OneToOne
    @NonNull
    private Photo authenticationPhoto;
}
