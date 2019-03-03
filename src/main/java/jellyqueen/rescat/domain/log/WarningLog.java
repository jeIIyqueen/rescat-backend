package jellyqueen.rescat.domain.log;

import jellyqueen.rescat.domain.BaseTime;
import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.enums.WarningType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class WarningLog extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private Long warningIdx;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private WarningType warningType;

    @OneToOne(fetch = FetchType.LAZY)
    private User warningUser;

}
