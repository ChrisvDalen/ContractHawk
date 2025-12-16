package com.contracthawk.controller;

import com.contracthawk.dto.ApiCreateDto;
import com.contracthawk.dto.EndpointCreateDto;
import com.contracthawk.entity.HttpMethod;
import com.contracthawk.entity.Lifecycle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiContractControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createApi_ShouldReturn201() throws Exception {
        ApiCreateDto dto = new ApiCreateDto();
        dto.setName("Test API");
        dto.setBaseUrl("https://api.example.com");
        dto.setVersion("v1.0.0");
        dto.setOwnerTeam("Team A");
        dto.setLifecycle(Lifecycle.DRAFT);
        
        mockMvc.perform(post("/api/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test API"))
                .andExpect(jsonPath("$.version").value("v1.0.0"));
    }
    
    @Test
    void createApi_WithValidationErrors_ShouldReturn400() throws Exception {
        ApiCreateDto dto = new ApiCreateDto();
        dto.setName(""); // Invalid: too short
        dto.setBaseUrl("https://api.example.com");
        dto.setVersion("v1.0.0");
        dto.setOwnerTeam("Team A");
        dto.setLifecycle(Lifecycle.DRAFT);
        
        mockMvc.perform(post("/api/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists());
    }
    
    @Test
    void getApi_NotFound_ShouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/apis/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
    
    @Test
    void addEndpoint_DuplicateMethodAndPath_ShouldReturn409() throws Exception {
        // Create API
        ApiCreateDto apiDto = new ApiCreateDto();
        apiDto.setName("Test API");
        apiDto.setBaseUrl("https://api.example.com");
        apiDto.setVersion("v1.0.0");
        apiDto.setOwnerTeam("Team A");
        apiDto.setLifecycle(Lifecycle.DRAFT);
        
        String apiResponse = mockMvc.perform(post("/api/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String apiId = objectMapper.readTree(apiResponse).get("id").asText();
        
        // Add first endpoint
        EndpointCreateDto endpointDto = new EndpointCreateDto();
        endpointDto.setMethod(HttpMethod.GET);
        endpointDto.setPath("/users");
        
        mockMvc.perform(post("/api/apis/" + apiId + "/endpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointDto)))
                .andExpect(status().isCreated());
        
        // Try to add duplicate endpoint
        mockMvc.perform(post("/api/apis/" + apiId + "/endpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }
}

