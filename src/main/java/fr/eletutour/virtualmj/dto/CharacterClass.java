package fr.eletutour.virtualmj.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CharacterClass {
    CLERC("clerc"),
    GUERRIER("guerrier"),
    MAGICIEN("magicien"),
    ROUBLARD("roublard");

    private final String value;

    CharacterClass(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CharacterClass fromString(String input) {
        if (input == null) return GUERRIER;

        String normalized = input.trim().toUpperCase();

        if (normalized.contains("GUERRIER") || normalized.contains("FIGHTER")) return GUERRIER;
        if (normalized.contains("CLERC") || normalized.contains("PRETRE")) return CLERC;
        if (normalized.contains("MAGICIEN") || normalized.contains("MAGE") || normalized.contains("SORCIER")) return MAGICIEN;
        if (normalized.contains("ROUBLARD") || normalized.contains("VOLEUR")) return ROUBLARD;

        try {
            return CharacterClass.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return GUERRIER;
        }
    }
}