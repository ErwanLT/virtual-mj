package fr.eletutour.virtualmj.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct; // Attention au package selon votre version Java/Spring
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        // 1. Récupération des fichiers
        Resource[] resources = resolver.getResources("classpath:rules/**/*.md");
        List<Document> rawDocuments = new ArrayList<>();

        for (Resource resource : resources) {
            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            // Petit check pour ne pas traiter des fichiers vides
            if (!content.isBlank()) {
                rawDocuments.add(new Document(
                        content,
                        Map.of("source", resource.getFilename())
                ));
            }
        }

        // 2. Configuration du découpage (C'est la partie MAGIQUE qui corrige l'erreur)
        // Le modèle mxbai a une limite stricte (souvent 512 tokens).
        // On découpe en blocs de 400 tokens pour être sûr (safe zone), avec un chevauchement de 100 pour le contexte.
        // Paramètres : (chunkSize, minChunkSize, minChunkLengthToEmbed, maxNumChunks, keepSeparator)
        // Si le constructeur varie selon votre version, utilisez new TokenTextSplitter() qui a des défauts souvent acceptables
        // Mais ici je force une petite taille pour éviter l'erreur 400.
        TokenTextSplitter splitter = new TokenTextSplitter(400, 200, 5, 10000, true);

        // 3. Transformation : 1 gros doc -> N petits docs
        List<Document> splitDocuments = splitter.apply(rawDocuments);

        // 4. Ingestion
        if (!splitDocuments.isEmpty()) {
            vectorStore.add(splitDocuments);
            log.info("Ingestion terminée : {} fichiers transformés en {} segment", rawDocuments.size(), splitDocuments.size());
        }
    }
}