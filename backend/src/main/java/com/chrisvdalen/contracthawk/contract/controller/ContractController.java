package com.chrisvdalen.contracthawk.contract.controller;

import com.chrisvdalen.contracthawk.analysis.application.ContractAnalysisResponse;
import com.chrisvdalen.contracthawk.contract.application.ContractDetailsResponse;
import com.chrisvdalen.contracthawk.contract.application.ContractListItem;
import com.chrisvdalen.contracthawk.contract.application.ContractQueryService;
import com.chrisvdalen.contracthawk.contract.application.ContractResponse;
import com.chrisvdalen.contracthawk.contract.application.ContractUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractUploadService uploadService;
    private final ContractQueryService queryService;

    public ContractController(ContractUploadService uploadService, ContractQueryService queryService) {
        this.uploadService = uploadService;
        this.queryService = queryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse upload(@RequestParam("serviceName") String serviceName,
                                   @RequestParam("version") String version,
                                   @RequestParam("file") MultipartFile file) {
        return uploadService.upload(serviceName, version, file);
    }

    @GetMapping
    public List<ContractListItem> list() {
        return queryService.listAll();
    }

    @GetMapping("/{id}")
    public ContractDetailsResponse get(@PathVariable Long id) {
        return queryService.getById(id);
    }

    @GetMapping("/{id}/analyses")
    public List<ContractAnalysisResponse> analyses(@PathVariable Long id) {
        return queryService.listAnalyses(id);
    }

    @GetMapping("/{id}/latest-analysis")
    public ContractAnalysisResponse latestAnalysis(@PathVariable Long id) {
        return queryService.getLatestAnalysis(id);
    }
}
