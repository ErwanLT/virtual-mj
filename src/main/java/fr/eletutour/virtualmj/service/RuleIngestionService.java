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
import org.yaml.snakeyaml.Yaml;

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

        for (Resource resource : uniqueResourcesMap.values()) {
            String filename = resource.getFilename();

            try {
                String rawContent = new String(
                        resource.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);

                if (rawContent.isBlank())
                    continue;

                // ─────────────────────────────────────────────
                // ÉTAPE 1 : PARSING FRONT-MATTER
                // ─────────────────────────────────────────────
                Map<String, Object> frontMatter = new HashMap<>();
                String body = rawContent;

                if (rawContent.startsWith("---")) {
                    int end = rawContent.indexOf("\n---", 3);
                    if (end > 0) {
                        String yamlBlock = rawContent.substring(3, end).trim();
                        body = rawContent.substring(end + 4).trim();

                        Yaml yaml = new Yaml();
                        Map<String, Object> parsed = yaml.load(yamlBlock);
                        if (parsed != null) {
                            frontMatter.putAll(parsed);
                            log.debug("Métadonnées extraites pour {} : {}", filename, frontMatter);
                        }
                    } else {
                        log.debug("Pas de bloc FrontMatter valide trouvé pour {}", filename);
                    }
                } else {
                    log.debug("Le fichier {} ne commence pas par FrontMatter (---)", filename);
                }

                if (body.isBlank()) {
                    log.warn("Contenu vide après extraction FrontMatter pour {}", filename);
                    continue;
                }

                // ─────────────────────────────────────────────
                // ÉTAPE 2 : SUPPRESSION DES ANCIENS CHUNKS
                // ─────────────────────────────────────────────
                removeDocumentsBySource(filename);

                // ─────────────────────────────────────────────
                // ÉTAPE 3 : DOCUMENT PARENT
                // ─────────────────────────────────────────────
                String parentId = UUID
                        .nameUUIDFromBytes(filename.getBytes(StandardCharsets.UTF_8))
                        .toString();

                Map<String, Object> baseMetadata = new HashMap<>(frontMatter);
                baseMetadata.put("source", filename);
                baseMetadata.put("parent_id", parentId);

                Document rawDoc = new Document(parentId, body, baseMetadata);

                // ─────────────────────────────────────────────
                // ÉTAPE 4 : SPLIT + IDS DÉTERMINISTES
                // ─────────────────────────────────────────────
                List<Document> splitDocs = splitter.apply(List.of(rawDoc));

                List<Document> docsToIngest = splitDocs.stream()
                        .map(doc -> {
                            String chunkContent = doc.getText();
                            int chunkIndex = Integer.parseInt(
                                    doc.getMetadata()
                                            .getOrDefault("chunk_index", "0")
                                            .toString());

                            String signature = filename + "::" + chunkIndex + "::" + chunkContent;
                            String chunkId = UUID
                                    .nameUUIDFromBytes(signature.getBytes(StandardCharsets.UTF_8))
                                    .toString();

                            Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                            metadata.putAll(frontMatter);

                            return new Document(chunkId, chunkContent, metadata);
                        })
                        .toList();

                if (!docsToIngest.isEmpty()) {
                    vectorStore.add(docsToIngest);
                    log.info("Fichier {} : {} segments ingérés.", filename, docsToIngest.size());
                    if (log.isDebugEnabled()) {
                        docsToIngest
                                .forEach(d -> log.debug("  -> Chunk {} ingéré. Meta: {}", d.getId(), d.getMetadata()));
                    }
                } else {
                    log.warn("Aucun segment généré pour {}", filename);
                }

            } catch (Exception e) {
                log.error("Erreur lors de l'ingestion de {}", filename, e);
            }
        }

        log.info("Ingestion des règles terminée.");
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
                log.debug("Suppression de {} anciens segments pour : {}", idsToDelete.size(), filename);
                vectorStore.delete(idsToDelete);
            } else {
                log.debug("Aucun ancien segment trouvé pour : {}", filename);
            }
        } catch (Exception e) {
            log.warn("Erreur suppression anciens docs pour {}", filename, e);
        }
    }
}