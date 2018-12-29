package com.sopt.rescat.dto;

import com.sopt.rescat.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long idx;
    private String nickName;
    private String contents;
    private LocalDateTime createdAt;
    private Role userRole;
}
