package jellyqueen.rescat.domain.enums;

public enum MarkerType {
    CAFETERIA(0),
    HOSPITAL(1),
    Cat(2);

    private Integer value;

    MarkerType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
