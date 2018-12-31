package com.sopt.rescat.domain.photo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.rescat.domain.BaseTime;
import com.sopt.rescat.domain.CarePost;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class CarePostPhoto extends BaseTime {
    public static final Long DEFAULT_PHOTO_ID = 1L;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String url;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_care_post_photo_care_post_restaurant"))
    @JsonIgnore
    private CarePost carePost;

    public CarePostPhoto(@NonNull String url) {
        this.url = url;
    }
}
