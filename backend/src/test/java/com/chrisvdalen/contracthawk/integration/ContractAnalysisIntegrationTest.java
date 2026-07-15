package com.chrisvdalen.contracthawk.integration;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import com.chrisvdalen.contracthawk.messaging.config.MessagingProperties;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ContractAnalysisIntegrationTest {

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

    @Autowired MockMvc mockMvc;
    @Autowired ContractRepository contractRepository;
    @Autowired ContractAnalysisRepository analysisRepository;
    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired MessagingProperties messagingProperties;

    @BeforeEach
    void cleanUp() {
        analysisRepository.deleteAll();
        contractRepository.deleteAll();
        drainQueue(messagingProperties.queue());
        drainQueue(messagingProperties.deadLetterQueue());
    }

    @Test
    void validContractReachesCompletedWithCountsPopulated() throws Exception {
        String spec = """
                openapi: 3.0.3
                info:
                  title: Orders
                  version: '1.0.0'
                paths:
                  /orders:
                    get:
                      responses:
                        '200':
                          description: ok
                    post:
                      responses:
                        '201':
                          description: created
                  /orders/{id}:
                    get:
                      parameters:
                        - name: id
                          in: path
                          required: true
                          schema:
                            type: string
                      responses:
                        '200':
                          description: ok
                """;

        upload("order-service", "1.0.0", "spec.yaml", spec.getBytes());

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<ContractAnalysis> all = analysisRepository.findAll();
            assertThat(all).hasSize(1);
            ContractAnalysis analysis = all.get(0);
            assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
            assertThat(analysis.getValidSpec()).isTrue();
            assertThat(analysis.getPathCount()).isEqualTo(2);
            assertThat(analysis.getOperationCount()).isEqualTo(3);
            assertThat(analysis.getStartedAt()).isNotNull();
            assertThat(analysis.getFinishedAt()).isNotNull();
        });
    }

    @Test
    void invalidContractReachesCompletedWithValidSpecFalse() throws Exception {
        upload("order-service", "1.0.0", "spec.yaml", "not a valid openapi document".getBytes());

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<ContractAnalysis> all = analysisRepository.findAll();
            assertThat(all).hasSize(1);
            ContractAnalysis analysis = all.get(0);
            assertThat(analysis.getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
            assertThat(analysis.getValidSpec()).isFalse();
            assertThat(analysis.getPathCount()).isZero();
            assertThat(analysis.getOperationCount()).isZero();
        });
    }

    private void upload(String serviceName, String version, String filename, byte[] body) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, MediaType.APPLICATION_OCTET_STREAM_VALUE, body);
        mockMvc.perform(multipart("/api/contracts")
                        .file(file)
                        .param("serviceName", serviceName)
                        .param("version", version))
                .andExpect(status().isCreated());
    }

    private void drainQueue(String queue) {
        while (rabbitTemplate.receive(queue, 100) != null) {
        }
    }
}
