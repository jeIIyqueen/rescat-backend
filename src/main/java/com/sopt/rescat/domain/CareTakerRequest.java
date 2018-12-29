package com.sopt.rescat.domain;

import lombok.Builder;
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
    @Length(max = 10)
    private String name;

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

    @Builder
    public CareTakerRequest(User user, Boolean isConfirmed, String name, String phone, Region mainRegion, Photo authenticationPhoto){
        super(user, isConfirmed);
        this.name = name;
        this.phone = phone;
        this.mainRegion = mainRegion;
        this.authenticationPhoto = authenticationPhoto;
    }

}