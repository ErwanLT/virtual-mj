package fr.eletutour.virtualmj.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class CharacterCreationTool {

    private static final Logger log = LoggerFactory.getLogger(CharacterCreationTool.class);
    private static final Random random = new Random();

    private final DiceTool diceTool;

    public CharacterCreationTool(DiceTool diceTool) {
        this.diceTool = diceTool;
    }

    @Tool(name = "characterCreator", description = "Crée un personnage avec une race, une classe et des caractéristiques générées aléatoirement.")
    public CharacterSheet createCharacter(
            @ToolParam(description = "La race du personnage (ex: Humain, Elfe, Nain).") String race,
            @ToolParam(description = "La classe du personnage (ex: Guerrier, Magicien).") String characterClass,
            @ToolParam(description = "Le nom du personnage. Si non fourni, un nom aléatoire sera généré.") String name
    ) {
        log.info("createCharacter method called with race: {}, class: {}, name: {}", race, characterClass, name);

        String characterName = (name == null || name.isBlank()) ? generateName(race) : name;

        // Generate Ability Scores using 4d6 drop lowest
        String method = "4d6kh3";
        List<Integer> abilityScores = generateAbilityScores();

        CharacterSheet sheet = new CharacterSheet(characterName, race, characterClass, abilityScores, method);
        log.info("createCharacter method finished with result: {}", sheet);
        return sheet;
    }

    private List<Integer> generateAbilityScores() {
        List<Integer> abilityScores = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            DiceResult result = diceTool.rollDice("d6", 4, 0);
            List<Integer> rolls = new ArrayList<>(result.rolls());
            Collections.sort(rolls, Collections.reverseOrder());
            int score = rolls.get(0) + rolls.get(1) + rolls.get(2);
            abilityScores.add(score);
        }
        return abilityScores;
    }

    private String generateName(String race) {
        List<String> names;
        if (race == null) {
            race = "humain";
        }
        switch (race.toLowerCase()) {
            case "elfe":
                names = List.of("Legolas", "Arwen", "Galadriel", "Elrond", "Thranduil");
                break;
            case "nain":
                names = List.of("Gimli", "Thorin", "Balin", "Dwalin", "Fili", "Kili");
                break;
            case "halfelin":
                names = List.of("Frodo", "Sam", "Merry", "Pippin", "Bilbo");
                break;
            case "humain":
            default:
                names = List.of("Aragorn", "Boromir", "Faramir", "Eowyn", "Theoden");
                break;
        }
        return names.get(random.nextInt(names.size()));
    }
}
