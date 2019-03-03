package jellyqueen.rescat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerDto {
    private Long idx;
    private String link;
    private String photoUrl;
    private String title;
}
