package com.contracthawk.service;

import com.contracthawk.entity.Endpoint;
import com.contracthawk.entity.HttpMethod;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiService {
    
    private final WebClient.Builder webClientBuilder;
    
    public List<Endpoint> parseOpenApiSpec(String openApiUrl) {
        try {
            // Validate URL
            URI uri = URI.create(openApiUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("OpenAPI URL must be http or https");
            }
            
            // Fetch spec
            String specContent = fetchOpenApiSpec(openApiUrl);
            
            // Parse with swagger-parser
            OpenAPIV3Parser parser = new OpenAPIV3Parser();
            SwaggerParseResult parseResult = parser.readContents(specContent, null, null);
            
            if (parseResult.getMessages() != null && !parseResult.getMessages().isEmpty()) {
                log.warn("OpenAPI parse warnings: {}", parseResult.getMessages());
            }
            
            OpenAPI openAPI = parseResult.getOpenAPI();
            if (openAPI == null) {
                throw new IllegalArgumentException("Failed to parse OpenAPI spec: " + 
                    (parseResult.getMessages() != null ? String.join(", ", parseResult.getMessages()) : "Unknown error"));
            }
            
            // Extract endpoints
            return extractEndpoints(openAPI);
            
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to fetch OpenAPI spec from " + openApiUrl + ": " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAPI spec: " + e.getMessage(), e);
        }
    }
    
    private String fetchOpenApiSpec(String url) {
        // Extract base URL for baseUrl, use full URL for request
        URI uri = URI.create(url);
        String baseUrl = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        String path = uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
        
        WebClient webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + ", " + MediaType.APPLICATION_YAML_VALUE)
                .build();
        
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }
    
    private List<Endpoint> extractEndpoints(OpenAPI openAPI) {
        List<Endpoint> endpoints = new ArrayList<>();
        
        if (openAPI.getPaths() == null) {
            return endpoints;
        }
        
        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();
            
            // Process each HTTP method
            processOperation(pathItem.getGet(), path, HttpMethod.GET, endpoints);
            processOperation(pathItem.getPost(), path, HttpMethod.POST, endpoints);
            processOperation(pathItem.getPut(), path, HttpMethod.PUT, endpoints);
            processOperation(pathItem.getPatch(), path, HttpMethod.PATCH, endpoints);
            processOperation(pathItem.getDelete(), path, HttpMethod.DELETE, endpoints);
        }
        
        return endpoints;
    }
    
    private void processOperation(Operation operation, String path, HttpMethod method, List<Endpoint> endpoints) {
        if (operation == null) {
            return;
        }
        
        String description = null;
        if (operation.getSummary() != null && !operation.getSummary().trim().isEmpty()) {
            description = operation.getSummary();
        } else if (operation.getDescription() != null && !operation.getDescription().trim().isEmpty()) {
            description = operation.getDescription();
        }
        
        boolean deprecated = Boolean.TRUE.equals(operation.getDeprecated());
        
        Endpoint endpoint = Endpoint.builder()
                .method(method)
                .path(path)
                .description(description)
                .deprecated(deprecated)
                .build();
        
        endpoints.add(endpoint);
    }
}

