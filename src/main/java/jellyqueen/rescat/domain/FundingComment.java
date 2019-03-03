package jellyqueen.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jellyqueen.rescat.domain.enums.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Getter
@Entity
@Slf4j
public class FundingComment extends BaseEntity {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(readOnly = true)
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

    @Column
    @Builder.Default
    @ApiModelProperty(readOnly = true)
    private int warning = 0;

    @Transient
    @ApiModelProperty(readOnly = true, notes = "닉네임")
    private String nickname;

    @Transient
    @ApiModelProperty(readOnly = true, notes = "유저 등급")
    private Role userRole;

    @Transient
    @ApiModelProperty(readOnly = true, notes = "작성자 일치 여부")
    private Boolean isWriter;

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

    private boolean equalsWriter(User loginUser) {
        return this.getWriter().equals(loginUser);
    }

    public FundingComment setStatus(User loginUser) {
        this.isWriter = this.equalsWriter(loginUser);
        return this;
    }

    public void warningCount() {
        ++this.warning;
    }

}
