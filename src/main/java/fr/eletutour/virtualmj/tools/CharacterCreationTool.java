package fr.eletutour.virtualmj.tools;

import fr.eletutour.virtualmj.dto.CharacterClass;
import fr.eletutour.virtualmj.dto.CharacterRace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CharacterCreationTool {

    private static final Logger log = LoggerFactory.getLogger(CharacterCreationTool.class);
    private static final Random random = new Random();

    // Index des caractéristiques : 0=STR, 1=DEX, 2=CON, 3=INT, 4=WIS, 5=CHA
    private static final int STR = 0, DEX = 1, CON = 2, INT = 3, WIS = 4, CHA = 5;

    private final DiceTool diceTool;

    public CharacterCreationTool(DiceTool diceTool) {
        this.diceTool = diceTool;
    }

    /**
     * Méthode principale appelée par le LLM via le callback.
     * Les types sont maintenant fortement typés (Enums).
     */
    public CharacterSheet createCharacter(CharacterRace race, String subRace, CharacterClass characterClass, String name) {
        log.info("createCharacter called with Enums: race={}, class={}, name={}", race, characterClass, name);

        // 1. Gestion des valeurs par défaut (sécurité)
        CharacterRace finalRace = (race != null) ? race : CharacterRace.HUMAIN;
        CharacterClass finalClass = (characterClass != null) ? characterClass : CharacterClass.GUERRIER;
        String finalName = (name == null || name.isBlank()) ? generateName(finalRace) : name;

        // 2. Génération des scores (4d6 keep high 3)
        List<Integer> rawScores = generateRawPool();

        // 3. Tri pour optimisation (du plus grand au plus petit)
        rawScores.sort(Collections.reverseOrder());

        // 4. Assignation intelligente selon la classe
        List<Integer> optimizedStats = assignStatsByClass(finalClass, rawScores);

        // 5. Création de la fiche
        // On utilise .getValue() pour avoir le nom propre ("nain" au lieu de "NAIN")
        CharacterSheet sheet = new CharacterSheet(
                finalName,
                finalRace.getValue(),
                subRace,
                finalClass.getValue(),
                optimizedStats,
                "4d6kh3 (Optimisé pour " + finalClass.getValue() + ")"
        );

        log.info("Character created: {}", sheet);
        return sheet;
    }

    private List<Integer> generateRawPool() {
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            // Lancer 4 dés de 6
            DiceResult result = diceTool.rollDice("d6", 4, 0);
            List<Integer> rolls = new ArrayList<>(result.rolls());
            // Garder les 3 meilleurs
            Collections.sort(rolls, Collections.reverseOrder());
            int score = rolls.get(0) + rolls.get(1) + rolls.get(2);
            pool.add(score);
        }
        return pool;
    }

    private List<Integer> assignStatsByClass(CharacterClass clazz, List<Integer> sortedScores) {
        Integer[] finalStats = new Integer[6];
        List<Integer> priorities = switch (clazz) {
            case GUERRIER ->
                // Force > Con > Dex
                    List.of(STR, CON, DEX, CHA, WIS, INT);
            case ROUBLARD ->
                // Dex > Con > Int/Cha
                    List.of(DEX, CON, INT, CHA, STR, WIS);
            case MAGICIEN ->
                // Int > Con > Dex
                    List.of(INT, CON, DEX, WIS, CHA, STR);
            case CLERC ->
                // Sag > Con > For
                    List.of(WIS, CON, STR, CHA, INT, DEX);
            default ->
                // Équilibré par défaut
                    List.of(STR, DEX, CON, INT, WIS, CHA);
        };

        // Définition des priorités selon l'Enum

        // Remplissage du tableau final
        for (int i = 0; i < 6; i++) {
            int statIndex = priorities.get(i);
            finalStats[statIndex] = sortedScores.get(i);
        }

        return Arrays.asList(finalStats);
    }

    private String generateName(CharacterRace race) {
        List<String> names = switch (race) {
            case ELFE -> List.of("Legolas", "Arwen", "Galadriel", "Thranduil", "Elrond");
            case NAIN -> List.of("Gimli", "Thorin", "Balin", "Gloin", "Durin");
            case HALFELIN -> List.of("Frodon", "Sam", "Merry", "Pippin", "Bilbo");
            default -> List.of("Aragorn", "Boromir", "Eowyn", "Faramir", "Isildur");
        };
        return names.get(random.nextInt(names.size()));
    }
}
