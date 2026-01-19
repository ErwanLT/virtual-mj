package fr.eletutour.virtualmj.llm;

import fr.eletutour.virtualmj.tools.CharacterCreationTool;
import fr.eletutour.virtualmj.tools.DiceTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final ChatClient chatClient;
    private final DiceTool diceTool;
    private final CharacterCreationTool characterCreationTool;

    public OllamaClient(ChatClient.Builder builder, DiceTool diceTool, CharacterCreationTool characterCreationTool) {
        this.chatClient = builder.build();
        this.diceTool = diceTool;
        this.characterCreationTool = characterCreationTool;
        log.info("OllamaClient initialized with tools: DiceTool, CharacterCreationTool");
    }

    public String chat(String prompt) {
        log.debug("Chat called with prompt: {}", prompt);
        log.debug("Registering tools: diceTool, characterCreationTool");

        String response = chatClient
                .prompt(prompt)
                .tools(diceTool, characterCreationTool)
                .call()
                .content();

        log.debug("Response received: {}", response);
        return response;
    }
}
