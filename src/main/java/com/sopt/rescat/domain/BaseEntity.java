package com.sopt.rescat.domain;

        import com.fasterxml.jackson.annotation.JsonIgnore;
        import lombok.AccessLevel;
        import lombok.AllArgsConstructor;
        import lombok.Getter;
        import lombok.NoArgsConstructor;

        import javax.persistence.Column;
        import javax.persistence.MappedSuperclass;
        import javax.persistence.OneToOne;

@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity extends BaseTime {
    @OneToOne
    @JsonIgnore
    private User writer;
}