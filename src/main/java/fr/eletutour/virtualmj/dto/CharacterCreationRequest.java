package fr.eletutour.virtualmj.dto;

public record CharacterCreationRequest(
        String name,
        CharacterRace race,
        String subRace,
        CharacterClass classe,
        String description) {

    @Override
    public String toString() {
        return "CharacterCreationRequest{" +
                "name='" + name + '\'' +
                ", race=" + race +
                ", subRace='" + subRace + '\'' +
                ", classe=" + classe +
                ", description='" + description + '\'' +
                '}';
    }
}
