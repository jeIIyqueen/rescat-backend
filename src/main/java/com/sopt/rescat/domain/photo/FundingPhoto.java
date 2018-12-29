package com.sopt.rescat.domain.photo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sopt.rescat.domain.BaseTime;
import com.sopt.rescat.domain.Funding;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class FundingPhoto extends BaseTime {
    public static final Long DEFAULT_PHOTO_ID = 1L;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String url;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_funding_photo_funding_idx"))
    private Funding funding;

    public FundingPhoto(@NonNull String url) {
        this.url = url;
    }
}
