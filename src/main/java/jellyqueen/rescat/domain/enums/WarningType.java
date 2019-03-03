package jellyqueen.rescat.domain.enums;

public enum WarningType {
    CAREPOST(0),
    CAREPOSTCOMMENT(1),
    FUNDING(2),
    FUNDINGCOMMENT(3);

    private Integer value;

    WarningType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}
