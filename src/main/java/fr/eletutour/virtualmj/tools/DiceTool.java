package fr.eletutour.virtualmj.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DiceTool {

    private static final Logger log = LoggerFactory.getLogger(DiceTool.class);

    @Tool(name = "diceTool", description = "Lance des dés pour le jeu de rôle (ex: 1d20+2)")
    public DiceResult rollDice(
            @ToolParam(description = "Type de dé, ex: d20, d6, d100") String dice,
            @ToolParam(description = "Nombre de dés à lancer") int count,
            @ToolParam(description = "Modificateur à ajouter au total") int modifier
    ) {
        log.info("rollDice method called with dice: {}, count: {}, modifier: {}", dice, count, modifier);
        int sides = Integer.parseInt(dice.substring(1));
        Random random = new Random();

        int total = 0;
        List<Integer> rolls = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int roll = random.nextInt(sides) + 1;
            rolls.add(roll);
            total += roll;
        }

        total += modifier;
        DiceResult result = new DiceResult(dice, rolls, modifier, total);
        log.info("rollDice method finished with result: {}", result);
        return result;
    }
}
