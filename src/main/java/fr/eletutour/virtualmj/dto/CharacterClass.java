package fr.eletutour.virtualmj.dto;

public enum CharacterClass {
    CLERC("clerc"),
    GUERRIER("guerrier"),
    MAGICIEN("magicien"),
    ROUBLARD("roublard");

    private final String value;

    CharacterClass(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
