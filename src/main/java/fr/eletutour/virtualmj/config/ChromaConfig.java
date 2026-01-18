package fr.eletutour.virtualmj.config;

import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChromaConfig {

    @Bean
    public VectorStore vectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel) {
        // Le 3ème paramètre est "initializeSchema".
        // On le force à 'false' pour empêcher Spring de vérifier/créer la collection au démarrage.
        // Puisque vous l'avez déjà créée via cURL, cela va "juste marcher".
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName("rules")
                .initializeSchema(false)
                .build();
    }
}