package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.llm.OllamaClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MjService {

    private final OllamaClient ollamaClient;
    private final RuleRagService ruleRagService;

    public MjService(OllamaClient ollamaClient,
            RuleRagService ruleRagService) {
        this.ollamaClient = ollamaClient;
        this.ruleRagService = ruleRagService;
    }

    public String play(String playerAction) {

        List<Document> rules = ruleRagService.findRelevantRules(playerAction);

        String rulesContext = rules.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                Tu es un maître du jeu Donjons & Dragons.
                Tu respectes strictement les règles ci-dessous.

                RÈGLES PERTINENTES :
                %s

                ACTION DU JOUEUR :
                %s

                Décide si un jet est nécessaire.
                Narre le résultat sans inventer de règles.
                """.formatted(rulesContext, playerAction);

        return ollamaClient.chat(prompt);
    }
}
