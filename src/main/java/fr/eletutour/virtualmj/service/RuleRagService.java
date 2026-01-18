package fr.eletutour.virtualmj.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleRagService {

    private final VectorStore vectorStore;

    public RuleRagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> findRelevantRules(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                                .topK(5)
                                .similarityThreshold(0.7)
                                .build()
        );
    }
}
