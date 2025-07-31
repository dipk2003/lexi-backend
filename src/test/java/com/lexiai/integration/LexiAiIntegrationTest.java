package com.lexiai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexiai.dto.AuthResponse;
import com.lexiai.dto.LoginRequest;
import com.lexiai.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.lexiai.LexiAiApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class LexiAiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeUserJourney() throws Exception {
        // 1. Check application health
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // 2. Check email availability
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "integration@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 3. Register new user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");
        registerRequest.setEmail("integration@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirmName("Test Integration Firm");
        registerRequest.setFirmEmail("firm@test.com");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract token from registration response
        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(registerResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // 4. Login with registered user
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");
        
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract new token from login response
        String loginResponse = loginResult.getResponse().getContentAsString();
        AuthResponse loginAuthResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String loginToken = loginAuthResponse.getToken();

        // 5. Get user profile
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.firstName").value("Integration"));

        // 6. Get search history (should be empty)
        mockMvc.perform(get("/api/user/search-history")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        // 7. Get search statistics
        mockMvc.perform(get("/api/user/search-stats")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSearches").value(0));

        // 8. Test popular cases endpoint
        mockMvc.perform(get("/api/search/cases/popular")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk());

        // 9. Test recent cases endpoint
        mockMvc.perform(get("/api/search/cases/recent")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk());

        // 10. Test unauthorized access
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());

        // 11. Test email not available after registration
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "integration@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void rateLimitingTest() throws Exception {
        // Register and get token first
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Rate");
        registerRequest.setLastName("Limit");
        registerRequest.setEmail("ratelimit@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirmName("Rate Limit Firm");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(registerResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Make multiple requests to test rate limiting
        // Note: Rate limit is set to 50 requests per minute, so this shouldn't trigger it
        // but tests the header is present
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Rate-Limit-Remaining"));
    }

    @Test
    void invalidAuthenticationTest() throws Exception {
        // Test invalid login
        LoginRequest invalidLogin = new LoginRequest("invalid@test.com", "wrongpassword");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Test invalid token
        mockMvc.perform(get("/api/user/profile")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());

        // Test missing token
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validationTest() throws Exception {
        // Test registration with invalid data
        RegisterRequest invalidRegister = new RegisterRequest();
        invalidRegister.setFirstName(""); // Empty first name
        invalidRegister.setLastName("");  // Empty last name
        invalidRegister.setEmail("invalid-email"); // Invalid email format
        invalidRegister.setPassword("123"); // Short password
        invalidRegister.setFirmName(""); // Empty firm name

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRegister)))
                .andExpect(status().isBadRequest());

        // Test login with invalid data
        LoginRequest invalidLogin = new LoginRequest("", "");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }
}
