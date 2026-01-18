package fr.eletutour.virtualmj.controller;

import fr.eletutour.virtualmj.service.MjService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MjController.class)
class MjControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MjService mjService;

    @Test
    @DisplayName("Should return narrate response")
    void shouldReturnNarrateResponse() throws Exception {
        String playerAction = "J'ouvre la porte";
        String narration = "La porte grince sinistrement...";

        when(mjService.play(anyString())).thenReturn(narration);

        mockMvc.perform(post("/api/mj/play")
                .content(playerAction))
                .andExpect(status().isOk())
                .andExpect(content().string(narration));
    }
}
