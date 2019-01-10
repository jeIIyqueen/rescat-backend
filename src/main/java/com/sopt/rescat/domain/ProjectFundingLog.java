package com.sopt.rescat.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Slf4j
public class ProjectFundingLog extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_project_funding_log_user_idx"))
    private User sponsor;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_project_funding_log_funding_idx"))
    private Funding funding;

    @Column
    @NonNull
    private Long amount;

    @Column
    @NonNull
    // 0: 기한 전, 1: 기한 완료되었으나 모금달성 실패로 환불, 2: 계좌전송
    private Integer status;

    public void refund(){
        if(status == 0){
            this.status = 1;
            this.sponsor.updateMileage(amount);
        }
    }
}
