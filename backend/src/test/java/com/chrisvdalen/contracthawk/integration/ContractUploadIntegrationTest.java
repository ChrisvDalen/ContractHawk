package com.chrisvdalen.contracthawk.integration;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import com.chrisvdalen.contracthawk.messaging.config.MessagingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ContractUploadIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("contracthawk")
            .withUsername("contracthawk")
            .withPassword("contracthawk");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ContractAnalysisRepository analysisRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MessagingProperties messagingProperties;

    @BeforeEach
    void cleanUp() {
        analysisRepository.deleteAll();
        contractRepository.deleteAll();
        drainQueue();
    }

    @Test
    void uploadValidContractReturns201AndCreatesPendingAnalysis() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "order-service.yaml",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "openapi: 3.0.3\ninfo:\n  title: Order\n  version: '1.0'\npaths: {}\n".getBytes());

        mockMvc.perform(multipart("/api/contracts")
                        .file(file)
                        .param("serviceName", "order-service")
                        .param("version", "1.0.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName").value("order-service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));

        assertThat(contractRepository.findAll()).hasSize(1);
        assertThat(analysisRepository.findAll())
                .singleElement()
                .satisfies(a -> assertThat(a.getStatus()).isEqualTo(AnalysisStatus.PENDING));

        Message message = rabbitTemplate.receive(messagingProperties.queue(), 5_000);
        assertThat(message).as("analysis job should be published").isNotNull();
    }

    @Test
    void uploadEmptyFileReturns400AndStoresNothing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.yaml",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/contracts")
                        .file(file)
                        .param("serviceName", "order-service")
                        .param("version", "1.0.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMPTY_FILE"));

        assertThat(contractRepository.count()).isZero();
        assertThat(analysisRepository.count()).isZero();
        assertThat(rabbitTemplate.receive(messagingProperties.queue(), 500)).isNull();
    }

    @Test
    void uploadUnsupportedExtensionReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "spec.txt",
                MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/api/contracts")
                        .file(file)
                        .param("serviceName", "order-service")
                        .param("version", "1.0.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_EXTENSION"));

        assertThat(contractRepository.count()).isZero();
        assertThat(analysisRepository.count()).isZero();
        assertThat(rabbitTemplate.receive(messagingProperties.queue(), 500)).isNull();
    }

    private void drainQueue() {
        while (rabbitTemplate.receive(messagingProperties.queue(), 100) != null) {
        }
    }
}
