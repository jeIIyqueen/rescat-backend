package com.sopt.rescat.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime {

    @ApiModelProperty(readOnly = true)
    @CreatedDate
    private LocalDateTime createdAt;

    @ApiModelProperty(readOnly = true)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    protected void initUpdatedAt() {
        this.updatedAt = now();
    }
}