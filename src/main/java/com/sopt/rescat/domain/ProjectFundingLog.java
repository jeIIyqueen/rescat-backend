package com.sopt.rescat.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
