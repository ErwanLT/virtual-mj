package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.tools.DiceTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class RuleRagService {

    private static final Logger log = LoggerFactory.getLogger(RuleRagService.class);

    private final VectorStore vectorStore;

    public RuleRagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> findRelevantRules(String query) {
        log.info("Finding relevant rules for query: {}", query);
        var rules = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .similarityThreshold(0.2)
                        .build()
        );
        List<Document> uniqueRules = rules.stream()
                .filter(distinctByKey(Document::getFormattedContent))
                .toList();
        log.info("Found {} relevant rules ({} after deduplication) for query: {}", rules.size(), uniqueRules.size(), query);
        return rules;
    }

    // Petite méthode utilitaire pour la déduplication
   public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
   }
}
