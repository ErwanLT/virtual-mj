package fr.eletutour.virtualmj.tools;

import java.util.List;

public record DiceResult(
        String dice,
        List<Integer> rolls,
        int modifier,
        int total
) {}
