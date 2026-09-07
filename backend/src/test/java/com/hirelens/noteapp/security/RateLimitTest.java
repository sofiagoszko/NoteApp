package com.hirelens.noteapp.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void blocksLoginAfterConfiguredAttemptsFromSameIp() throws Exception {
        String body = "{\"email\":\"nobody@x.com\",\"password\":\"whatever\"}";
        String ip = "203.0.113.7";

        // capacity = 5
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/users/login").header("X-Real-IP", ip)
                    .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }


        mockMvc.perform(post("/api/users/login").header("X-Real-IP", ip)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
