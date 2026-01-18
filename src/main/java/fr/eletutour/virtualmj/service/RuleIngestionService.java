package fr.eletutour.virtualmj.service;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RuleIngestionService {

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resolver;

    public RuleIngestionService(VectorStore vectorStore,
                                ResourcePatternResolver resolver) {
        this.vectorStore = vectorStore;
        this.resolver = resolver;
    }

    @PostConstruct
    public void ingestRules() throws IOException {
        Resource[] resources =
                resolver.getResources("classpath:rules/**/*.md");

        List<Document> documents = new ArrayList<>();

        for (Resource resource : resources) {
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            documents.add(new Document(
                    content,
                    Map.of("source", resource.getFilename())
            ));
        }

        vectorStore.add(documents);
    }
}
