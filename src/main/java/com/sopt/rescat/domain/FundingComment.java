package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class FundingComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(length = 300)
    @NonNull
    private String contents;

    @Column
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_funding_idx"))
    @JsonIgnore
    private Funding funding;

    @Transient
    private String nickname;

    @Transient
    private Role userRole;

    public FundingComment setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public FundingComment setUserRole() {
        this.userRole = getWriter().getRole();
        return this;
    }
}
