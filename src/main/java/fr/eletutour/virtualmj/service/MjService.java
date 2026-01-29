package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.dto.CharacterCreationRequest;
import fr.eletutour.virtualmj.llm.OllamaClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MjService {

    private final OllamaClient ollamaClient;
    private final RuleRagService ruleRagService;

    @Value("classpath:/prompts/play-prompt.st")
    private Resource playPromptResource;

    @Value("classpath:/prompts/create-character-prompt.st")
    private Resource createCharacterPromptResource;

    public MjService(OllamaClient ollamaClient, RuleRagService ruleRagService) {
        this.ollamaClient = ollamaClient;
        this.ruleRagService = ruleRagService;
    }

    public String play(String playerAction) {
        List<Document> rules = ruleRagService.findRelevantRules(playerAction);
        String rulesContext = rules.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        PromptTemplate promptTemplate = new PromptTemplate(playPromptResource);
        Map<String, Object> model = Map.of(
                "rulesContext", rulesContext,
                "playerAction", playerAction);
        String prompt = promptTemplate.render(model);

        return ollamaClient.chat(prompt);
    }

    public String createCharacter(CharacterCreationRequest request) {

        // 1. RAG CIBLÉ : On cherche spécifiquement du "Lore"
        // On utilise la méthode qui filtre par métadonnées
        List<Document> rules = ruleRagService.findRelevantRules(request);

        String rulesContext = rules.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 2. DESCRIPTION PROPRE (Format "Phrase Naturelle")
        String description = String.format(
                "Le joueur veut créer un personnage. Nom: %s. Race: %s. Sous-race: %s. Classe: %s. Description: %s",
                request.name(),
                request.race().getValue(),
                (request.subRace() != null ? request.subRace() : "Non spécifiée"),
                request.classe().getValue(),
                request.description()
        );

        // 3. PRÉPARATION DU PROMPT
        PromptTemplate promptTemplate = new PromptTemplate(createCharacterPromptResource);
        Map<String, Object> model = Map.of(
                "rulesContext", rulesContext,
                "description", description);
        String prompt = promptTemplate.render(model);

        // 4. OPTIONS AUTORITAIRES
        return ollamaClient.chat(prompt);
    }
}