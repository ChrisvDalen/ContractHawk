package com.chrisvdalen.contracthawk.repository;

import com.chrisvdalen.contracthawk.analysis.domain.AnalysisStatus;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.domain.Contract;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ContractRepositoriesTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("contracthawk")
            .withUsername("contracthawk")
            .withPassword("contracthawk");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    ContractAnalysisRepository analysisRepository;

    @BeforeEach
    void clean() {
        analysisRepository.deleteAll();
        contractRepository.deleteAll();
    }

    @Test
    void contractLookupByNameAndExcludingIdReturnsMostRecentOtherVersion() {
        OffsetDateTime base = OffsetDateTime.now().minusSeconds(2);
        Contract v1 = contractRepository.save(new Contract("svc", "1.0.0", "a.yaml", "/s/1.0.0/a.yaml", base));
        Contract v2 = contractRepository.save(new Contract("svc", "2.0.0", "b.yaml", "/s/2.0.0/b.yaml", base.plusSeconds(1)));
        Contract other = contractRepository.save(new Contract("other", "1.0.0", "c.yaml", "/o/1.0.0/c.yaml", base));

        assertThat(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("svc", v2.getId()))
                .hasValueSatisfying(c -> assertThat(c.getId()).isEqualTo(v1.getId()));
        assertThat(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("svc", v1.getId()))
                .hasValueSatisfying(c -> assertThat(c.getId()).isEqualTo(v2.getId()));
        assertThat(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("svc", other.getId()))
                .hasValueSatisfying(c -> assertThat(c.getId()).isEqualTo(v2.getId()));
        assertThat(contractRepository.findTopByServiceNameAndIdNotOrderByUploadedAtDesc("unknown", v1.getId()))
                .isEmpty();
    }

    @Test
    void analysisLookupOrderByCreatedAtReturnsMostRecentFirst() {
        Contract contract = contractRepository.save(
                new Contract("svc", "1.0.0", "a.yaml", "/s/1.0.0/a.yaml", OffsetDateTime.now()));

        OffsetDateTime base = OffsetDateTime.now().minusSeconds(2);
        ContractAnalysis first = analysisRepository.save(ContractAnalysis.pending(contract.getId(), base));
        ContractAnalysis second = analysisRepository.save(ContractAnalysis.pending(contract.getId(), base.plusSeconds(1)));

        List<ContractAnalysis> all = analysisRepository.findByContractIdOrderByCreatedAtDesc(contract.getId());
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(all.get(0).getId()).isEqualTo(second.getId());
        assertThat(all.get(1).getId()).isEqualTo(first.getId());

        assertThat(analysisRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId()))
                .hasValueSatisfying(a -> assertThat(a.getId()).isEqualTo(second.getId()));
        assertThat(analysisRepository.findTopByContractIdOrderByCreatedAtDesc(999L)).isEmpty();
    }

    @Test
    void batchLookupReturnsOnlyAnalysesForGivenContracts() {
        OffsetDateTime now = OffsetDateTime.now();
        Contract c1 = contractRepository.save(new Contract("svc1", "1.0.0", "a.yaml", "/s1/1.0.0/a.yaml", now));
        Contract c2 = contractRepository.save(new Contract("svc2", "1.0.0", "b.yaml", "/s2/1.0.0/b.yaml", now));
        Contract c3 = contractRepository.save(new Contract("svc3", "1.0.0", "c.yaml", "/s3/1.0.0/c.yaml", now));

        analysisRepository.save(ContractAnalysis.pending(c1.getId(), now));
        analysisRepository.save(ContractAnalysis.pending(c1.getId(), now));
        analysisRepository.save(ContractAnalysis.pending(c2.getId(), now));

        List<ContractAnalysis> batch = analysisRepository.findByContractIdIn(List.of(c1.getId(), c2.getId(), c3.getId()));
        assertThat(batch).hasSize(3);
        assertThat(batch).extracting(ContractAnalysis::getContractId)
                .containsExactlyInAnyOrder(c1.getId(), c1.getId(), c2.getId());

        assertThat(analysisRepository.findByContractIdIn(List.of())).isEmpty();
    }
}
