package fr.eletutour.virtualmj.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RuleIngestionService {

    private final Logger log = LoggerFactory.getLogger(RuleIngestionService.class);

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resolver;

    public RuleIngestionService(VectorStore vectorStore,
                                ResourcePatternResolver resolver) {
        this.vectorStore = vectorStore;
        this.resolver = resolver;
    }

    @PostConstruct
    public void ingestRules() throws IOException {
        log.info("Démarrage de l'ingestion des règles...");

        // 1. Récupération des fichiers (Déduplication)
        Resource[] resources = resolver.getResources("classpath:rules/**/*.md");
        Map<String, Resource> uniqueResourcesMap = new HashMap<>();
        for (Resource resource : resources) {
            if (resource.getFilename() != null) {
                uniqueResourcesMap.put(resource.getFilename(), resource);
            }
        }
        TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 5, 10000, true);
        int totalChunks = 0;

        for (Resource resource : uniqueResourcesMap.values()) {
            String filename = resource.getFilename();
            try {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (content.isBlank()) continue;

                // --- ETAPE 1 : ID DU PARENT DÉTERMINISTE ---
                // On génère un ID unique pour le fichier lui-même basé sur son nom
                String parentId = UUID.nameUUIDFromBytes(filename.getBytes(StandardCharsets.UTF_8)).toString();

                // On force cet ID dans le constructeur
                Document rawDoc = new Document(parentId, content, Map.of("source", filename));

                // --- ETAPE 2 : NETTOYAGE PRÉALABLE ---
                // On supprime AVANT de découper ou de traiter quoi que ce soit
                removeDocumentsBySource(filename);

                // --- ETAPE 3 : DÉCOUPAGE ET ID DES CHUNKS ---
                List<Document> splitDocuments = splitter.apply(List.of(rawDoc));

                List<Document> docsToIngest = splitDocuments.stream()
                        .map(doc -> {
                            // ID du chunk = Hash(NomFichier + Index du chunk + Contenu)
                            // L'ajout de l'index garantit l'unicité même si le texte se répète
                            String chunkContent = doc.getText();
                            int chunkIndex = Integer.parseInt(doc.getMetadata().getOrDefault("chunk_index", "0").toString());

                            String signature = filename + "::" + chunkIndex + "::" + chunkContent;
                            String deterministicId = UUID.nameUUIDFromBytes(signature.getBytes(StandardCharsets.UTF_8)).toString();

                            // On recrée le doc avec l'ID forcé
                            return new Document(deterministicId, chunkContent, new HashMap<>(doc.getMetadata()));
                        })
                        .toList();

                // --- ETAPE 4 : INGESTION ---
                if (!docsToIngest.isEmpty()) {
                    vectorStore.add(docsToIngest);
                    log.info("Fichier {} : {} segments mis à jour.", filename, docsToIngest.size());
                }

            } catch (Exception e) {
                log.error("Erreur sur {}", filename, e);
            }
        }
        log.info("Ingestion terminée. Total segments actifs : {}", totalChunks);
    }

    private void removeDocumentsBySource(String filename) {
        try {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            SearchRequest request = SearchRequest.builder()
                    .query(" ")
                    .filterExpression(b.eq("source", filename).build())
                    .topK(1000)
                    .build();

            List<Document> existingDocs = vectorStore.similaritySearch(request);

            if (!existingDocs.isEmpty()) {
                List<String> idsToDelete = existingDocs.stream().map(Document::getId).toList();
                vectorStore.delete(idsToDelete);
            }
        } catch (Exception e) {
            log.warn("Erreur suppression anciens docs pour {}", filename, e);
        }
    }
}