package jellyqueen.rescat.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@Getter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity extends BaseTime {
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ApiModelProperty(readOnly = true)
    private User writer;

    protected void initWriter(User writer) {
        this.writer = writer;
    }
}
