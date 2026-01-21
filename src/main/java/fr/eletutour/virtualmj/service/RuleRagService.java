package fr.eletutour.virtualmj.service;

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

@Service
public class RuleRagService {

    private static final Logger log = LoggerFactory.getLogger(RuleRagService.class);
    private final VectorStore vectorStore;

    public RuleRagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> findRelevantRules(String query) {
        log.info("Finding relevant rules for query: {}", query);

        // 1. STRATÉGIE "OVERSHOOT" :
        // On demande 20 résultats (topK) pour être sûr d'avoir assez de matière
        // même si la base contient beaucoup de doublons (zombies).
        var rules = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(20)
                        .similarityThreshold(0.4)
                        .build()
        );

        // 2. DÉDUPLICATION INTELLIGENTE :
        // On filtre sur getText() (le texte brut).
        // Ainsi, "Règle A" (avec ID parent X) et "Règle A" (avec ID parent Y) sont considérées identiques.
        List<Document> uniqueRules = rules.stream()
                .filter(distinctByKey(Document::getText))
                .limit(5) // On ne garde que les 5 meilleurs UNIQUES
                .toList();

        log.info("Found {} raw hits, returning {} unique rules for query: {}", rules.size(), uniqueRules.size(), query);

        return uniqueRules;
    }

    // Utilitaire de déduplication (inchangé)
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
