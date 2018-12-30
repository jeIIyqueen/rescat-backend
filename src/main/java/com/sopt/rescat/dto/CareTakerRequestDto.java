package com.sopt.rescat.dto;

import com.sopt.rescat.domain.CareTakerRequest;
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
@Builder
public class CareTakerRequestDto {

    private final Integer CONFIRM = 1;
    private final Integer DEFER   = 0;
    private final Integer REFUSE  = 2;

    private String name;
    private String phone;
    private Integer emdCode;
    private MultipartFile authenticationPhoto;

    public boolean hasAuthenticationPhoto() {
        return authenticationPhoto != null;
    }

    public CareTakerRequest toCareTakerRequest(User user, Region mainRegion, String authenticationPhotoUrl) {
        return CareTakerRequest.builder()
                .user(user)
                .isConfirmed(DEFER)
                .name(name)
                .phone(phone)
                .mainRegion(mainRegion)
                .authenticationPhotoUrl(authenticationPhotoUrl)
                .build();
    }
}
