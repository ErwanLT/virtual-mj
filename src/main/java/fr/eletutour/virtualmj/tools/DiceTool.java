package fr.eletutour.virtualmj.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DiceTool {

    public DiceResult rollDice(
            @ToolParam(description = "Type de dé, ex: d20, d6, d100")
            String dice,

            @ToolParam(description = "Nombre de dés")
            int count,

            @ToolParam(description = "Modificateur à ajouter")
            int modifier
    ) {
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

        return new DiceResult(dice, rolls, modifier, total);
    }
}
