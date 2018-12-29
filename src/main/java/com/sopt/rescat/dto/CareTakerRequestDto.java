package com.sopt.rescat.dto;

import com.sopt.rescat.domain.CareTakerRequest;
import com.sopt.rescat.domain.Photo;
import com.sopt.rescat.domain.Region;
import com.sopt.rescat.domain.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CareTakerRequestDto {
    private String name;
    private String phone;
    private Region mainRegion;
    private MultipartFile authenticationPhoto;

    public CareTakerRequest toCareTakerRequest(User user, Photo authenticationPhoto) {
        return CareTakerRequest.builder()
                .user(user)
                .isConfirmed(false)
                .name(name)
                .phone(phone)
                .mainRegion(mainRegion)
                .authenticationPhoto(authenticationPhoto)
                .build();
    }
}
