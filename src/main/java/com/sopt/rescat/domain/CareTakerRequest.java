package com.sopt.rescat.domain;

import com.sopt.rescat.dto.CareTakerRequestDto;
import lombok.Builder;
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
    @Length(max = 10)
    private String name;

    @Column
    @NonNull
    @Length(max = 11)
    private String phone;

    @OneToOne
    @NonNull
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_care_taker_request_region_idx"))
    private Region mainRegion;

    @Column
    @NonNull
    private String authenticationPhotoUrl;

    @Column
    private Integer isConfirmed;

    @Builder
    public CareTakerRequest(User user, Integer isConfirmed, String name, String phone, Region mainRegion, String authenticationPhotoUrl){
        super(user);
        this.isConfirmed = isConfirmed;
        this.name = name;
        this.phone = phone;
        this.mainRegion = mainRegion;
        this.authenticationPhotoUrl = authenticationPhotoUrl;
    }

}