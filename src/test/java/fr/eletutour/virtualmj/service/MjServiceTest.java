package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.llm.OllamaClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mjService, "playPromptResource", new ClassPathResource("prompts/play-prompt.st"));
        ReflectionTestUtils.setField(mjService, "createCharacterPromptResource",
                new ClassPathResource("prompts/create-character-prompt.st"));
    }

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
    @DisplayName("Should create character correctly")
    void shouldCreateCharacter() {
        // Given
        fr.eletutour.virtualmj.dto.CharacterCreationRequest request = new fr.eletutour.virtualmj.dto.CharacterCreationRequest(
                "Gimli",
                fr.eletutour.virtualmj.dto.CharacterRace.NAIN,
                "Montagne",
                fr.eletutour.virtualmj.dto.CharacterClass.GUERRIER,
                "Un nain robuste");
        String expectedResponse = "Fiche de personnage...";

        when(ruleRagService.findRelevantRules(request))
                .thenReturn(List.of(new Document("Règles nains...")));

        when(ollamaClient.chat(anyString())).thenReturn(expectedResponse);

        // When
        String result = mjService.createCharacter(request);

        // Then
        assertEquals(expectedResponse, result);
        verify(ruleRagService).findRelevantRules(request);
        verify(ollamaClient).chat(anyString());
    }
}
