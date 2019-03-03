package jellyqueen.rescat.domain;

import jellyqueen.rescat.dto.response.BannerDto;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class FundingBanner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private String photoUrl;

    @Column
    private String title;

    @Column
    private String link;

    public BannerDto toBannerDto() {
        return BannerDto.builder()
                .idx(idx)
                .link(link)
                .photoUrl(photoUrl)
                .title(title)
                .build();
    }
}
