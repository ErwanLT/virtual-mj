package fr.eletutour.virtualmj.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleRagServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RuleRagService ruleRagService;

    @Test
    @DisplayName("Should find relevant rules for character creation with filtering")
    void shouldFindRelevantRulesForCharacterCreation() {
        // Given
        fr.eletutour.virtualmj.dto.CharacterCreationRequest request = new fr.eletutour.virtualmj.dto.CharacterCreationRequest(
                "Legolas",
                fr.eletutour.virtualmj.dto.CharacterRace.ELFE,
                "Sylvestre",
                fr.eletutour.virtualmj.dto.CharacterClass.ROUBLARD,
                "Un elfe agile");

        Document elfRule = new Document("Règle Elfe",
                java.util.Map.of("domain", "character", "topic", "creation", "race", "elfe"));
        Document dwarfRule = new Document("Règle Nain",
                java.util.Map.of("domain", "character", "topic", "creation", "race", "nain"));
        Document classRule = new Document("Règle Roublard",
                java.util.Map.of("domain", "character", "topic", "creation", "class", "roublard"));

        // Mock returns mixed results
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(elfRule, dwarfRule, classRule));

        // When
        List<Document> result = ruleRagService.findRelevantRules(request);

        // Then
        // Should keep Elf (matches race) and Rogue (matches class) but NOT Dwarf (wrong
        // race)
        assertEquals(2, result.size());
        assert (result.contains(elfRule));
        assert (result.contains(classRule));
        assert (!result.contains(dwarfRule));
    }
}
