package jellyqueen.rescat.domain.photo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jellyqueen.rescat.domain.BaseTime;
import jellyqueen.rescat.domain.Funding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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
    @JsonIgnore
    private Funding funding;

    public FundingPhoto(@NonNull String url) {
        this.url = url;
    }

    public FundingPhoto initFunding(Funding funding) {
        this.funding = funding;
        return this;
    }
}
