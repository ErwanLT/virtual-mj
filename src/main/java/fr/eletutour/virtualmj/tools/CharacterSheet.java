package fr.eletutour.virtualmj.tools;

import java.util.List;

public record CharacterSheet(
        String name,
        String race,
        String characterClass,
        List<Integer> abilityScores,
        String generationMethod
) {}
