package fr.eletutour.virtualmj.config;

import fr.eletutour.virtualmj.tools.CharacterCreationRequest;
import fr.eletutour.virtualmj.tools.CharacterCreationTool;
import fr.eletutour.virtualmj.tools.CharacterSheet;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ToolsConfiguration {

    @Bean
    public ToolCallback characterCreatorCallback(CharacterCreationTool tool) {
        return FunctionToolCallback
                .builder("characterCreator", (Function<CharacterCreationRequest, CharacterSheet>) request ->
                        tool.createCharacter(
                                request.race(),
                                request.subRace(),
                                request.characterClass(),
                                request.name()
                        ))
                .description("Crée un personnage D&D avec des caractéristiques optimisées selon la classe.")
                .inputType(CharacterCreationRequest.class)
                .build();
    }
}