package fr.eletutour.virtualmj.service;

import fr.eletutour.virtualmj.dto.CharacterCreationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
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
        return findRelevantRulesInternal(query, null, null);
    }

    public List<Document> findRelevantRules(CharacterCreationRequest request) {
        log.info("Finding relevant rules for character creation: {}", request);
        String enrichedQuery = String.format("création personnage %s %s", request.race().getValue(),
                request.job().getValue());
        return findRelevantRulesInternal(enrichedQuery, request.race().getValue(), request.job().getValue());
    }

    private List<Document> findRelevantRulesInternal(String query, String raceFilter, String classFilter) {
        log.info("Internal search: query='{}', race='{}', class='{}'", query, raceFilter, classFilter);

        // 1. Recherche large (overshoot volontaire)
        List<Document> rawResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(200)
                        .similarityThreshold(0.25)
                        .build());

        // 2. Filtrage "Jekyll-like" par métadonnées SI PRÉSENTES
        List<Document> filtered = rawResults.stream()
                .filter(doc -> isRelevantForCharacterCreation(doc, raceFilter, classFilter))
                .toList();

        // 3. Déduplication (limit removed)
        List<Document> uniqueRules = filtered.stream()
                .filter(distinctByKey(Document::getText))
                .toList();

        log.info(
                "Found {} raw hits, {} after metadata filtering, returning {} unique rules",
                rawResults.size(),
                filtered.size(),
                uniqueRules.size());

        return uniqueRules;
    }

    private boolean isRelevantForCharacterCreation(Document doc, String raceFilter, String classFilter) {
        Map<String, Object> meta = doc.getMetadata();

        // Si pas de métadonnées → on garde (fallback sécurité)
        if (meta == null || meta.isEmpty()) {
            log.debug("Document kept (no metadata): {}", doc.getId());
            return true;
        }

        String domain = (String) meta.get("domain");
        String topic = (String) meta.get("topic");

        // Si ce n'est pas un doc de création, on ignore si on est en mode création
        // (impliqué par le fait qu'on appelle cette méthode, mais bon)
        // Ici on garde la logique précédente : si domain != character ou topic !=
        // creation -> false
        // SAUF que findRelevantRules(String) appelle aussi cette méthode.
        // On va assouplir : si on a des filtres (race/classe), on devient strict.

        if (raceFilter != null || classFilter != null) {
            // Mode Création Strict

            // 1. Doit être character/creation
            if (domain != null && !domain.equalsIgnoreCase("character")) {
                log.debug("Document filtered out (wrong domain): {} - domain={}", doc.getId(), domain);
                return false;
            }
            // topic peut être "creation" ou null, mais si c'est "combat" c pas bon.
            if (topic != null && !topic.equalsIgnoreCase("creation")) {
                log.debug("Document filtered out (wrong topic): {} - topic={}", doc.getId(), topic);
                return false;
            }

            // 2. Filtre Race
            String docRace = (String) meta.get("race");
            if (raceFilter != null && docRace != null && !docRace.equalsIgnoreCase(raceFilter)) {
                log.debug("Document filtered out (wrong race): {} - docRace={} vs filter={}", doc.getId(), docRace,
                        raceFilter);
                return false;
            }

            // 3. Filtre Classe
            String docClass = (String) meta.get("class");
            if (classFilter != null && docClass != null && !docClass.equalsIgnoreCase(classFilter)) {
                log.debug("Document filtered out (wrong class): {} - docClass={} vs filter={}", doc.getId(), docClass,
                        classFilter);
                return false;
            }

            log.debug("Document kept (matched filters): {}", doc.getId());
            return true;
        } else {
            // Mode "Play" (String query) - comportement "Legacy"
            if (domain != null && !domain.equalsIgnoreCase("character")) {
                return false;
            }
            if (topic != null && !topic.equalsIgnoreCase("creation")) {
                return false;
            }
            return true;
        }
    }

    // Déduplication inchangée
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
