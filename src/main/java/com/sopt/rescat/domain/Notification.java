package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Notification extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private String contents;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RequestType targetType;

    @Column
    private Long targetIdx;

    public boolean isTargetIdxNull() {
        return this.targetIdx == null;
    }
}
