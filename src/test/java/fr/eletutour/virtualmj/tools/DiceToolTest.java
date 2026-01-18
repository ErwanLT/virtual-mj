package fr.eletutour.virtualmj.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiceToolTest {

    private final DiceTool diceTool = new DiceTool();

    @Test
    @DisplayName("Should roll correct number of dice")
    void shouldRollCorrectNumberOfDice() {
        DiceResult result = diceTool.rollDice("d6", 3, 0);

        assertNotNull(result);
        assertEquals("d6", result.dice());
        assertEquals(3, result.rolls().size());
        assertEquals(0, result.modifier());
    }

    @Test
    @DisplayName("Should apply modifier correctly")
    void shouldApplyModifier() {
        DiceResult result = diceTool.rollDice("d20", 1, 5);

        assertNotNull(result);
        int sumOfRolls = result.rolls().stream().mapToInt(Integer::intValue).sum();
        assertEquals(sumOfRolls + 5, result.total());
        assertEquals(5, result.modifier());
    }

    @Test
    @DisplayName("Should respect dice sides bounds")
    void shouldRespectDiceSides() {
        // Roll a d1 many times to ensure logic holds (should always be 1)
        DiceResult result = diceTool.rollDice("d1", 100, 0);

        for (int roll : result.rolls()) {
            assertEquals(1, roll);
        }
    }

    @Test
    @DisplayName("Should handle d100")
    void shouldHandleD100() {
        DiceResult result = diceTool.rollDice("d100", 5, 0);

        assertEquals(5, result.rolls().size());
        for (int roll : result.rolls()) {
            assertTrue(roll >= 1 && roll <= 100);
        }
    }
}
