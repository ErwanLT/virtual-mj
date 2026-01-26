package fr.eletutour.virtualmj.dto;

public record CharacterCreationRequest(
        String name,
        CharacterRace race,
        String subRace,
        CharacterClass job,
        String description) {
}
