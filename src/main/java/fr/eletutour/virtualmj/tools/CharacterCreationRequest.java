package fr.eletutour.virtualmj.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import fr.eletutour.virtualmj.dto.CharacterClass;
import fr.eletutour.virtualmj.dto.CharacterRace;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("Les paramètres de création du personnage.")
public record CharacterCreationRequest(

        @JsonPropertyDescription("La race PRINCIPALE uniquement (choisir strictement dans la liste : NAIN, ELFE, etc.).")
        @JsonProperty(required = true)
        CharacterRace race,

        @JsonPropertyDescription("La sous-race si précisée (ex: Nain des Montagnes, Haut-Elfe).")
        String subRace,

        @JsonPropertyDescription("La classe du personnage.")
        @JsonProperty(required = true)
        CharacterClass characterClass,

        @JsonPropertyDescription("Le nom du personnage.")
        String name
) {}