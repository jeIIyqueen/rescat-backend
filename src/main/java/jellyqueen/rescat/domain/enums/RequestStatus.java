package jellyqueen.rescat.domain.enums;

public enum RequestStatus {
    DEFER(0),
    CONFIRM(1),
    REFUSE(2);

    private Integer value;

    RequestStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
