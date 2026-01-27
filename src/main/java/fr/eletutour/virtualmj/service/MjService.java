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
        List<Document> rules = ruleRagService.findRelevantRules(request);
        String rulesContext = rules.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        PromptTemplate promptTemplate = new PromptTemplate(createCharacterPromptResource);
        Map<String, Object> model = Map.of(
                "rulesContext", rulesContext,
                "description", request.toString());
        String prompt = promptTemplate.render(model);

        return ollamaClient.chat(prompt);
    }
}