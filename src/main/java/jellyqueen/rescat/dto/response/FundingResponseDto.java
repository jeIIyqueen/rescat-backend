package jellyqueen.rescat.dto.response;

import jellyqueen.rescat.domain.photo.FundingPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FundingResponseDto {

    @ApiModelProperty(readOnly = true, notes = "펀딩 글 번호")
    private Long idx;
    @ApiModelProperty(notes = "글 제목")
    private String title;
    @ApiModelProperty(notes = "자기소개")
    private String introduction;
    @ApiModelProperty(notes = "이름")
    private String name;
    @ApiModelProperty(notes = "전화번호")
    private String phone;
    @ApiModelProperty(notes = "목표금액")
    private Long goalAmount;
    @ApiModelProperty(notes = "현재금액")
    private Long currentAmount;
    @ApiModelProperty(notes = "펀딩글 유형(0: 치료비 모금, 1: 프로젝트 후원)")
    private Integer category;
    @ApiModelProperty(notes = "대표 사진")
    private FundingPhoto mainPhoto;
    @ApiModelProperty(notes = "마감 기한")
    private LocalDateTime limitAt;
}
