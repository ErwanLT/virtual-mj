package fr.eletutour.virtualmj.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CharacterRace {
    ELFE("elfe"),
    HALFELIN("halfelin"),
    HUMAIN("humain"),
    NAIN("nain");

    private final String value;

    CharacterRace(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Méthode magique qui intercepte la désérialisation.
     * Elle permet de gérer les cas où le LLM est trop bavard.
     */
    @JsonCreator
    public static CharacterRace fromString(String input) {
        if (input == null) return HUMAIN; // Valeur par défaut de sécurité

        String normalized = input.trim().toUpperCase();

        // Logique floue pour rattraper les erreurs du LLM
        if (normalized.contains("NAIN")) return NAIN;
        if (normalized.contains("ELF")) return ELFE;
        if (normalized.contains("HALF") || normalized.contains("LIN")) return HALFELIN;
        if (normalized.contains("HUMAIN") || normalized.contains("HOMME")) return HUMAIN;

        // Si vraiment on ne trouve rien, on renvoie une valeur par défaut ou on laisse planter
        try {
            return CharacterRace.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return HUMAIN; // Fallback ultime
        }
    }
}