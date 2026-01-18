package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.llm.OllamaClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MjServiceTest {

    @Mock
    private OllamaClient ollamaClient;

    @Mock
    private RuleRagService ruleRagService;

    @InjectMocks
    private MjService mjService;

    @Test
    @DisplayName("Should play turn correctly")
    void shouldPlayTurn() {
        // Given
        String playerAction = "J'attaque le gobelin";
        String expectedResponse = "Vous lancez votre dague...";

        when(ruleRagService.findRelevantRules(playerAction))
                .thenReturn(List.of(new Document("Règles de combat...")));

        when(ollamaClient.chat(anyString())).thenReturn(expectedResponse);

        // When
        String result = mjService.play(playerAction);

        // Then
        assertEquals(expectedResponse, result);
        verify(ruleRagService).findRelevantRules(playerAction);
        verify(ollamaClient).chat(anyString());
    }

    @Test
    @DisplayName("Should handle empty rules")
    void shouldHandleEmptyRules() {
        // Given
        String playerAction = "Je danse";
        String expectedResponse = "Vous dansez avec grâce.";

        when(ruleRagService.findRelevantRules(playerAction))
                .thenReturn(List.of());
        when(ollamaClient.chat(anyString())).thenReturn(expectedResponse);

        // When
        String result = mjService.play(playerAction);

        // Then
        assertEquals(expectedResponse, result);
    }
}
