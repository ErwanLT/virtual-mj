package fr.eletutour.virtualmj.llm;

import fr.eletutour.virtualmj.tools.DiceTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class OllamaClient {

    private final ChatClient chatClient;

    public OllamaClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String prompt) {
        return chatClient
                .prompt(prompt)
                .toolNames("diceTool")
                .call()
                .content();
    }
}
