package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sopt.rescat.domain.enums.Role;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NonNull;

import javax.persistence.*;

@Getter
@Entity
public class CarePostComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String contents;

    @Column
    private String photoUrl;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_comment_care_post_idx"))
    @JsonIgnore
    private CarePost carePost;

    @Transient
    @ApiModelProperty(readOnly = true)
    private String nickname;

    @Transient
    @ApiModelProperty(readOnly = true)
    private Role userRole;

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
}
