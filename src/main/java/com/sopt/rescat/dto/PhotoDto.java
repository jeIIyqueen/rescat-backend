package com.sopt.rescat.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    @ApiModelProperty(example = "rescat.png")
    private String photoUrl;

}
