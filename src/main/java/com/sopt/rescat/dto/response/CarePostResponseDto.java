package com.sopt.rescat.dto.response;

import com.sopt.rescat.domain.photo.CarePostPhoto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CarePostResponseDto {
    @ApiModelProperty(readOnly = true, notes = "글번호")
    private Long idx;
    @ApiModelProperty(notes = "고양이 이름", required = true)
    private String name;
    @ApiModelProperty(notes = "글 내용", required = true)
    private String contents;
    @ApiModelProperty(notes = "사진")
    private CarePostPhoto photo;
    @ApiModelProperty(notes = "글 타입(0: 입양, 1: 임시보호)", required = true)
    // 0: 입양, 1: 임시보호
    private Integer type;
    @ApiModelProperty(notes = "조회수", required = true)
    private int viewCount;
    @ApiModelProperty(notes = "작성시간", required = true)
    private LocalDateTime createdAt;
    @ApiModelProperty(notes = "끌올시간", required = true)
    private LocalDateTime updatedAt;
    @ApiModelProperty(notes = "완료여부", required = true)
    private Boolean isFinished;
}
