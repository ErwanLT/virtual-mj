package fr.eletutour.virtualmj.dto;

public record CharacterCreationRequest(
        String name,
        CharacterRace race,
        String subRace,
        CharacterClass job,
        String description) {

    @Override
    public String toString() {
        return "CharacterCreationRequest{" +
                "name='" + name + '\'' +
                ", race=" + race +
                ", subRace='" + subRace + '\'' +
                ", job=" + job +
                ", description='" + description + '\'' +
                '}';
    }
}
