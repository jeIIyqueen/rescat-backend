package jellyqueen.rescat.domain.enums;

public enum Vaccination {
    UNKNOWINGNESS("모름", 0),
    NOTHING("안함", 1),
    FIRST("1차", 2),
    SECOND("2차", 3),
    THIRD("3차", 4);

    private String value;
    private int index;

    Vaccination(String value) {
        this.value = value;
    }


    Vaccination(String value, int index) {

    }

    public String getValue() {
        return value;
    }
}
