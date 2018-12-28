package com.sopt.rescat.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Photo extends BaseTime {
    public static final Long DEFAULT_PHOTO_ID = 1L;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    @NonNull
    private String url;

    public Photo(@NonNull String url) {
        this.url = url;
    }
}
