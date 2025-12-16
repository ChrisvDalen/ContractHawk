package com.contracthawk.service;

import com.contracthawk.dto.ImportResultDto;
import com.contracthawk.entity.*;
import com.contracthawk.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OpenApiSyncServiceTest {
    
    @Autowired
    private OpenApiSyncService openApiSyncService;
    
    @MockBean
    private OpenApiService openApiService;
    
    @Autowired
    private ApiContractRepository apiContractRepository;
    
    @Autowired
    private EndpointRepository endpointRepository;
    
    @Autowired
    private ApiSyncRunRepository apiSyncRunRepository;
    
    private ApiContract testApi;
    
    @BeforeEach
    void setUp() {
        testApi = ApiContract.builder()
                .name("Test API")
                .baseUrl("https://api.example.com")
                .version("v1.0.0")
                .ownerTeam("Test Team")
                .lifecycle(Lifecycle.ACTIVE)
                .openApiUrl("https://api.example.com/openapi.json")
                .build();
        testApi = apiContractRepository.save(testApi);
    }
    
    @Test
    void testImportOpenApi_MERGE_AddsNewEndpoints() {
        // Setup: Mock OpenAPI service to return new endpoints
        List<Endpoint> specEndpoints = new ArrayList<>();
        Endpoint newEndpoint = Endpoint.builder()
                .method(HttpMethod.GET)
                .path("/users")
                .description("Get users")
                .deprecated(false)
                .build();
        specEndpoints.add(newEndpoint);
        
        when(openApiService.parseOpenApiSpec(anyString())).thenReturn(specEndpoints);
        
        // Execute
        ImportResultDto result = openApiSyncService.importOpenApi(testApi.getId(), SyncMode.MERGE);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(0, result.getDeletedCount());
        assertFalse(result.getBreaksDetected());
        
        // Verify endpoint was created
        List<Endpoint> endpoints = endpointRepository.findByApiContractId(testApi.getId());
        assertEquals(1, endpoints.size());
        assertEquals("/users", endpoints.get(0).getPath());
    }
    
    @Test
    void testImportOpenApi_REPLACE_DeletesMissingEndpoints() {
        // Setup: Create existing endpoint
        Endpoint existing = Endpoint.builder()
                .apiContract(testApi)
                .method(HttpMethod.GET)
                .path("/old-endpoint")
                .description("Old endpoint")
                .deprecated(false)
                .build();
        endpointRepository.save(existing);
        
        // Mock OpenAPI service to return different endpoint
        List<Endpoint> specEndpoints = new ArrayList<>();
        Endpoint newEndpoint = Endpoint.builder()
                .method(HttpMethod.GET)
                .path("/new-endpoint")
                .description("New endpoint")
                .deprecated(false)
                .build();
        specEndpoints.add(newEndpoint);
        
        when(openApiService.parseOpenApiSpec(anyString())).thenReturn(specEndpoints);
        
        // Execute
        ImportResultDto result = openApiSyncService.importOpenApi(testApi.getId(), SyncMode.REPLACE);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.getAddedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(1, result.getDeletedCount());
        assertTrue(result.getBreaksDetected());
        assertFalse(result.getBreakingChanges().isEmpty());
        
        // Verify old endpoint was deleted
        List<Endpoint> endpoints = endpointRepository.findByApiContractId(testApi.getId());
        assertEquals(1, endpoints.size());
        assertEquals("/new-endpoint", endpoints.get(0).getPath());
    }
    
    @Test
    void testImportOpenApi_InvalidUrl_ThrowsException() {
        // Setup: API without openApiUrl
        ApiContract apiWithoutUrl = ApiContract.builder()
                .name("API Without URL")
                .baseUrl("https://api.example.com")
                .version("v1.0.0")
                .ownerTeam("Test Team")
                .lifecycle(Lifecycle.ACTIVE)
                .build();
        ApiContract savedApi = apiContractRepository.save(apiWithoutUrl);
        final UUID apiId = savedApi.getId();
        
        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            openApiSyncService.importOpenApi(apiId, SyncMode.MERGE);
        });
    }
}

