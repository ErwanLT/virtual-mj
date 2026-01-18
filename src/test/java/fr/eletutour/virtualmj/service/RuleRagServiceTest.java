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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleRagServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RuleRagService ruleRagService;

    @Test
    @DisplayName("Should find relevant rules")
    void shouldFindRelevantRules() {
        // Given
        String query = "combat";
        List<Document> expectedDocs = List.of(new Document("Règles de combat"));

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(expectedDocs);

        // When
        List<Document> result = ruleRagService.findRelevantRules(query);

        // Then
        assertEquals(expectedDocs, result);
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }
}
