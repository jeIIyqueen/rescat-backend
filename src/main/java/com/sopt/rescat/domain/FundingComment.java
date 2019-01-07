package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.rescat.domain.enums.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Getter
@Entity
public class FundingComment extends BaseEntity {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String contents;

    @Column
    @URL
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_funding_idx"))
    @JsonIgnore
    private Funding funding;

    @Transient
    @ApiModelProperty(readOnly = true)
    private String nickname;

    @Transient
    @ApiModelProperty(readOnly = true)
    private Role userRole;

    public FundingComment setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public FundingComment setUserRole() {
        this.userRole = getWriter().getRole();
        return this;
    }

    public FundingComment setWriter(User loginUser) {
        initWriter(loginUser);
        return this;
    }

    public FundingComment initFunding(Funding funding) {
        this.funding = funding;
        return this;
    }
}
