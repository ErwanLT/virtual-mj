package fr.eletutour.virtualmj.dto;

public enum CharacterRace {
    ELFE("elfe"),
    HALFELIN("halfelin"),
    HUMAIN("humain"),
    NAIN("nain");

    private final String value;

    CharacterRace(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
