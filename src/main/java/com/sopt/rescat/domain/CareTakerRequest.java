package com.sopt.rescat.domain;

import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;

@Entity
public class CareTakerRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    @Length(max = 11)
    private String phone;

    @OneToOne
    @NonNull
    private Region mainRegion;

    @OneToOne
    private Region subRegion1;

    @OneToOne
    private Region subRegion2;

    @OneToOne
    @NonNull
    private Photo authenticationPhoto;
}