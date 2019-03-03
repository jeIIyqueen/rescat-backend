package jellyqueen.rescat.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    @ApiModelProperty(example = "rescat.png")
    private String photoUrl;

}
