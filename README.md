# Virtual MJ

Virtual MJ est une application backend conçue pour agir comme un maître du jeu (MJ) virtuel pour des jeux de rôle. Elle utilise un grand modèle de langage (LLM) pour générer des narrations et interagir avec les joueurs, ainsi que des outils pour la création de personnages et le lancer de dés.

## Technologies utilisées

*   **Java 21**
*   **Spring Boot 3** : Framework principal de l'application.
*   **Spring AI** : Pour l'intégration avec les modèles de langage (LLM).
*   **Ollama** : Pour faire tourner localement le LLM.
*   **Chroma** : Base de données vectorielle pour le RAG (Retrieval-Augmented Generation).
*   **Maven** : Pour la gestion des dépendances et le build du projet.

## Retrieval-Augmented Generation (RAG)

Pour fournir des réponses précises et contextuelles sur les règles du jeu, l'application utilise une approche RAG. Les documents contenant les règles sont chargés au démarrage de l'application dans la base de données vectorielle Chroma.

Les documents utilisés sont tous les fichiers Markdown (`.md`) situés dans le répertoire :
`src/main/resources/rules/`

Vous pouvez ajouter, modifier ou supprimer des fichiers dans ce répertoire pour changer la base de connaissances du MJ virtuel.

### Pourquoi Markdown et non PDF ?

Le choix du format Markdown pour les documents de règles présente plusieurs avantages par rapport au PDF :
- **Simplicité de traitement** : Le Markdown est un format texte brut, ce qui rend son analyse (parsing) beaucoup plus simple et fiable pour l'ingestion dans une base de données vectorielle. L'extraction de texte à partir de PDF est souvent complexe et peut produire des erreurs de formatage.
- **Facilité d'édition** : Les fichiers Markdown peuvent être modifiés avec n'importe quel éditeur de texte, ce qui simplifie grandement la mise à jour et la correction des règles.
- **Légèreté et performance** : Les fichiers `.md` sont légers et leur traitement est plus rapide.
- **Suivi des versions** : En tant que format texte, Markdown est parfaitement adapté aux systèmes de contrôle de version comme Git, permettant de suivre facilement les modifications.


## Comment compiler et lancer le projet

### Prérequis

*   Java 21 ou supérieur.
*   Maven.
*   Ollama doit être installé et en cours d'exécution avec un modèle (par exemple, Llama3).

### Compilation

Pour compiler le projet et créer un jar exécutable, lancez la commande suivante à la racine du projet :

```bash
mvn clean package
```

### Lancement

Une fois le projet compilé, vous pouvez le lancer avec la commande :

```bash
java -jar target/virtual-mj-0.0.1-SNAPSHOT.jar
```

L'application démarrera par défaut sur le port 8080.

## API Endpoints

L'application expose une API REST pour interagir avec le MJ virtuel.

### `POST /api/mj/play`

Permet d'envoyer une action du joueur au MJ et de recevoir la narration en retour.

**Request Body:**

```json
{
  "playerAction": "J'attaque le gobelin avec mon épée."
}
```

**Response Body:**

```json
{
  "narration": "Vous levez votre épée et frappez le gobelin. Il pousse un cri perçant alors que votre lame le touche..."
}
```

### `POST /api/mj/create-character`

Permet de demander la création d'une fiche de personnage à partir d'une description.

**Request Body:**

```json
{
  "description": "Un guerrier nain robuste avec une grande hache."
}
```

**Response Body:**

Le service retournera une fiche de personnage sous forme de texte.
```
═══════════════════════════════════════
        FICHE DE PERSONNAGE D&D
═══════════════════════════════════════

NOM : Gurdill
RACE : Nain
CLASSE : Guerrier

CARACTÉRISTIQUES :
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Force         : 12 (+1)
Dextérité     : 13 (+1)
Constitution  : 11 (+0)
Intelligence  : 15 (+2)
Sagesse       : 11 (+0)
Charisme      : 11 (+0)

POINTS DE VIE : 11 (dé de vie + modificateur de Constitution)
```
