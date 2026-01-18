package fr.eletutour.virtualmj.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MjService {

    private final ChatClient chatClient;
    private final RuleRagService ruleRagService;

    public MjService(ChatClient chatClient,
                     RuleRagService ruleRagService) {
        this.chatClient = chatClient;
        this.ruleRagService = ruleRagService;
    }

    public String play(String playerAction) {

        List<Document> rules =
                ruleRagService.findRelevantRules(playerAction);

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

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
}
