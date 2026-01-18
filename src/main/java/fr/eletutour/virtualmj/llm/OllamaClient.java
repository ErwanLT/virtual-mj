package fr.eletutour.virtualmj.llm;

import fr.eletutour.virtualmj.tools.DiceTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaClient {

    private final ChatClient chatClient;
    private final DiceTool diceTool;

    public OllamaClient(ChatClient.Builder builder, DiceTool diceTool) {
        this.chatClient = builder.build();
        this.diceTool = diceTool;
    }

    public String chat(String prompt) {
        return chatClient
                .prompt(prompt)
                .tools(diceTool)
                .call()
                .content();
    }
}
