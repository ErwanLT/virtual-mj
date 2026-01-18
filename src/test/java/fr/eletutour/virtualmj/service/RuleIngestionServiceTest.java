package fr.eletutour.virtualmj.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleIngestionServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ResourcePatternResolver resolver;

    @Mock
    private Resource resource;

    @InjectMocks
    private RuleIngestionService ruleIngestionService;

    @Test
    @DisplayName("Should ingest rules correctly")
    void shouldIngestRules() throws IOException {
        // Given
        when(resolver.getResources(anyString())).thenReturn(new Resource[] { resource });
        when(resource.getInputStream())
                .thenReturn(new ByteArrayInputStream("Regles test".getBytes(StandardCharsets.UTF_8)));
        when(resource.getFilename()).thenReturn("test.md");

        // When
        ruleIngestionService.ingestRules();

        // Then
        verify(vectorStore).add(anyList());
        verify(resolver).getResources(anyString());
    }
}
