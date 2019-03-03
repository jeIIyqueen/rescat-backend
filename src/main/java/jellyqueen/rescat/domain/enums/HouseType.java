package jellyqueen.rescat.domain.enums;

public enum HouseType {
    // 0: 아파트, 1: 주택, 2: 다세대주택, 3: 원룸
    APARTMENT(0),
    HOUSING(1),
    MULTIFLEX(2),
    ONEROOM(3);

    private Integer value;

    HouseType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
