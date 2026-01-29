package fr.eletutour.virtualmj.tools;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CharacterSheet(
        String name,
        String race,
        String subRace,
        String characterClass,
        List<Integer> abilityScores,
        String generationMethod
) {
    // Ordre standard D&D
    private static final List<String> STAT_NAMES = List.of(
            "Force", "Dextérité", "Constitution",
            "Intelligence", "Sagesse", "Charisme"
    );

    /**
     * Cette méthode sera sérialisée en JSON et vue par le LLM.
     * Elle transforme la liste brute en un texte explicite.
     */
    public String getFormattedStats() {
        if (abilityScores == null || abilityScores.size() != 6) {
            return "Stats invalides";
        }

        return IntStream.range(0, 6)
                .mapToObj(i -> {
                    int score = abilityScores.get(i);
                    int modifier = (score - 10) / 2; // Calcul entier Java (arrondi vers le bas automatiquement)
                    String sign = modifier >= 0 ? "+" : "";
                    // Ex: "Force: 15 (+2)"
                    return String.format("%s: %d (%s%d)", STAT_NAMES.get(i), score, sign, modifier);
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * Calcul des PV de base (simplifié pour l'exemple : 10 + modif Constitution)
     * Le LLM a souvent du mal à lier la classe au dé de vie, on peut l'aider ici.
     */
    public String getEstimatedHitPoints() {
        if (abilityScores == null || abilityScores.size() < 3) return "Inconnu";

        int conScore = abilityScores.get(2); // Index 2 = Constitution
        int conMod = (conScore - 10) / 2;

        // Logique simplifiée : on assume une moyenne de 8 pour le dé de vie + Con
        int hp = 8 + conMod;
        return String.format("%d (Basé sur moyenne classe + Modif Con %d)", hp, conMod);
    }
}
