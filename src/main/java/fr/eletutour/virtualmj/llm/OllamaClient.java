package fr.eletutour.virtualmj.llm;

import fr.eletutour.virtualmj.tools.CharacterCreationTool;
import fr.eletutour.virtualmj.tools.DiceTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private final ChatClient chatClient;

    // On injecte l'interface générique ToolCallback
    private final ToolCallback characterCreatorCallback;

    public OllamaClient(ChatClient.Builder builder, ToolCallback characterCreatorCallback) {
        this.chatClient = builder.build();
        this.characterCreatorCallback = characterCreatorCallback;
    }

    public String chat(String prompt) {
        log.debug("Chat called with prompt length: {}", prompt);

        String response = chatClient
                .prompt(prompt)
                .toolCallbacks(characterCreatorCallback)
                .options(OllamaChatOptions.builder()
                        .toolNames("characterCreator")
                        .temperature(0.0)
                        .build())
                .call()
                .content();

        log.debug("Response received:\n {}", response);
        return response;
    }
}
