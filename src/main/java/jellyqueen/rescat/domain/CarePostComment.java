package jellyqueen.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jellyqueen.rescat.domain.enums.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;

@Getter
@Entity
public class CarePostComment extends BaseEntity {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(readOnly = true)
    private Long idx;

    @Column
    @NonNull
    private String contents;

    @Column
    @URL
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_care_post_idx"))
    @JsonIgnore
    private CarePost carePost;

    @Column
    @Builder.Default
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

    public CarePostComment setWriterNickname() {
        this.nickname = getWriter().getNickname();
        return this;
    }

    public CarePostComment setUserRole() {
        this.userRole = getWriter().getRole();
        return this;
    }

    public CarePostComment setWriter(User writer) {
        initWriter(writer);
        return this;
    }

    public CarePostComment initCarePost(CarePost carePost) {
        this.carePost = carePost;
        return this;
    }

    private boolean equalsWriter(User loginUser) {
        return this.getWriter().equals(loginUser);
    }

    public CarePostComment setStatus(User loginUser) {
        this.isWriter = this.equalsWriter(loginUser);
        return this;
    }

    public void warningCount() {
        ++this.warning;
    }

}
