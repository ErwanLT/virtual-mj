package fr.eletutour.virtualmj.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@Configuration
public class DiceToolConfig {

    // 1. On définit un objet (Record) qui contient TOUS les paramètres.
    // C'est beaucoup plus facile pour le LLM de remplir un seul objet JSON.
    public record DiceRequest(String dice, int count, int modifier) {}

    // 2. Le résultat (Record aussi)
    public record DiceResult(String dice, List<Integer> rolls, int modifier, int total) {}

    // 3. La fonction exposée comme Bean
    @Bean("diceTool") // <--- Le nom exact à utiliser dans .functions()
    @Description("Lance des dés pour le jeu de rôle. Exemple : pour 1d20+2, dice='d20', count=1, modifier=2")
    public Function<DiceRequest, DiceResult> diceFunction() {
        return request -> {
            // Logique métier
            String diceStr = request.dice().startsWith("d") ? request.dice().substring(1) : request.dice();
            int sides = Integer.parseInt(diceStr);
            
            Random random = new Random();
            int total = 0;
            List<Integer> rolls = new ArrayList<>();

            for (int i = 0; i < request.count(); i++) {
                int roll = random.nextInt(sides) + 1;
                rolls.add(roll);
                total += roll;
            }
            total += request.modifier();

            return new DiceResult(request.dice(), rolls, request.modifier(), total);
        };
    }
}