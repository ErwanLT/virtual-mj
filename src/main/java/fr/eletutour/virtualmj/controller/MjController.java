package fr.eletutour.virtualmj.controller;

import fr.eletutour.virtualmj.service.MjService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mj")
public class MjController {

    private final MjService mjService;

    public MjController(MjService mjService) {
        this.mjService = mjService;
    }

    @PostMapping("/play")
    public ResponseEntity<String> play(@RequestBody PlayRequest request) {
        String narrate = mjService.play(request.playerAction());
        return ResponseEntity.ok(narrate);
    }

    public record PlayRequest(String playerAction) {
    }
}
