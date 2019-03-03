package jellyqueen.rescat.domain.log;

import jellyqueen.rescat.domain.BaseEntity;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.enums.RequestStatus;
import jellyqueen.rescat.domain.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
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
