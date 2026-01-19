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

    public String createCharacter(String characterDescription) {
        List<Document> rules = ruleRagService.findRelevantRules("création de personnage " + characterDescription);

        String rulesContext = rules.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                Tu es un maître du jeu Donjons & Dragons.
                Tu dois créer une fiche de personnage complète pour le joueur.
                Tu respectes strictement les règles ci-dessous.

                RÈGLES PERTINENTES :
                %s

                DESCRIPTION DU PERSONNAGE PAR LE JOUEUR :
                %s

                Crée maintenant la fiche de personnage avec toutes les caractéristiques (Force, Dextérité, Constitution, Intelligence, Sagesse, Charisme).
                Si des informations comme la race ou la classe sont manquantes, choisis-en une appropriée.
                Si le nom n'est pas donné, génère-en un qui correspond à la race.
                """
                .formatted(rulesContext, characterDescription);

        return ollamaClient.chat(prompt);
    }
}
