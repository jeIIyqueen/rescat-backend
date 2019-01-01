package com.sopt.rescat.domain;

import com.sopt.rescat.domain.enums.RequestStatus;
import com.sopt.rescat.domain.enums.RequestType;
import lombok.Builder;

import javax.persistence.*;

@Builder
@Entity
public class ApprovalLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private Long requestIdx;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RequestStatus requestStatus;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private RequestType requestType;

    public ApprovalLog setApprover(User approver) {
        initWriter(approver);
        return this;
    }
}
