package com.sopt.rescat.dto;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class CareTakerRequestDto {
    private String name;
    private String phone;
    private Region mainRegion;
    private Integer emdCode;
    private MultipartFile authenticationPhoto;
    private String authenticationPhotoUrl;

    public CareTakerRequest toCareTakerRequest(User user, Region mainRegion, String authenticationPhotoUrl) {
        return CareTakerRequest.builder()
                .user(user)
                .isConfirmed(false)
                .name(name)
                .phone(phone)
                .mainRegion(mainRegion)
                .authenticationPhotoUrl(authenticationPhotoUrl)
                .build();
    }
}
