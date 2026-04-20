package com.chrisvdalen.contracthawk.contract.application;

import com.chrisvdalen.contracthawk.analysis.application.ContractAnalysisResponse;
import com.chrisvdalen.contracthawk.analysis.domain.ContractAnalysis;
import com.chrisvdalen.contracthawk.analysis.repository.ContractAnalysisRepository;
import com.chrisvdalen.contracthawk.contract.domain.Contract;
import com.chrisvdalen.contracthawk.contract.repository.ContractRepository;
import com.chrisvdalen.contracthawk.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ContractQueryService {

    private final ContractRepository contractRepository;
    private final ContractAnalysisRepository analysisRepository;

    public ContractQueryService(ContractRepository contractRepository, ContractAnalysisRepository analysisRepository) {
        this.contractRepository = contractRepository;
        this.analysisRepository = analysisRepository;
    }

    public List<ContractListItem> listAll() {
        return contractRepository.findAll().stream()
                .map(this::toListItem)
                .toList();
    }

    public ContractDetailsResponse getById(Long id) {
        Contract contract = findContract(id);
        return ContractDetailsResponse.from(contract);
    }

    public List<ContractAnalysisResponse> listAnalyses(Long contractId) {
        ensureContractExists(contractId);
        return analysisRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(ContractAnalysisResponse::from)
                .toList();
    }

    public ContractAnalysisResponse getLatestAnalysis(Long contractId) {
        ensureContractExists(contractId);
        return analysisRepository.findTopByContractIdOrderByCreatedAtDesc(contractId)
                .map(ContractAnalysisResponse::from)
                .orElseThrow(() -> new NotFoundException("ANALYSIS_NOT_FOUND",
                        "No analysis exists for contract " + contractId));
    }

    private Contract findContract(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CONTRACT_NOT_FOUND", "Contract " + id + " not found"));
    }

    private void ensureContractExists(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new NotFoundException("CONTRACT_NOT_FOUND", "Contract " + id + " not found");
        }
    }

    private ContractListItem toListItem(Contract contract) {
        Optional<ContractAnalysis> latest = analysisRepository.findTopByContractIdOrderByCreatedAtDesc(contract.getId());
        return new ContractListItem(
                contract.getId(),
                contract.getServiceName(),
                contract.getVersion(),
                contract.getOriginalFilename(),
                contract.getUploadedAt(),
                latest.map(ContractAnalysis::getStatus).orElse(null),
                latest.map(ContractAnalysis::getBreakingChangesDetected).orElse(null));
    }
}
