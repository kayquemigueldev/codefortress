package com.codefortress.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codefortress.shared.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SystemStatusController.class)
@Import(SecurityConfiguration.class)
class SystemStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPublicSystemStatus() throws Exception {
        mockMvc.perform(get("/api/v1/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("CodeFortress API"))
                .andExpect(jsonPath("$.status").value("operational"))
                .andExpect(jsonPath("$.stage").value("foundation"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}