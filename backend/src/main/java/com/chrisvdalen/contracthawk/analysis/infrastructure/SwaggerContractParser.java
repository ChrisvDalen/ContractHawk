package com.chrisvdalen.contracthawk.analysis.infrastructure;

import com.chrisvdalen.contracthawk.analysis.application.ContractParser;
import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SwaggerContractParser implements ContractParser {

    @Override
    public ParsedContract parse(InputStream content) throws IOException {
        String body;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8))) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }

        SwaggerParseResult result;
        try {
            result = new OpenAPIV3Parser().readContents(body, null, null);
        } catch (RuntimeException e) {
            return new ParsedContract(false, 0, 0, List.of("Parse error: " + e.getMessage()));
        }

        List<String> messages = Optional.ofNullable(result.getMessages()).orElse(List.of());
        OpenAPI openApi = result.getOpenAPI();

        if (openApi == null || openApi.getPaths() == null) {
            return new ParsedContract(false, 0, 0, messages);
        }

        int pathCount = openApi.getPaths().size();
        int operationCount = openApi.getPaths().values().stream()
                .mapToInt(SwaggerContractParser::countOperations)
                .sum();

        boolean valid = messages.isEmpty();
        return new ParsedContract(valid, pathCount, operationCount, messages);
    }

    private static int countOperations(PathItem item) {
        if (item == null) {
            return 0;
        }
        int count = 0;
        if (item.getGet() != null) count++;
        if (item.getPost() != null) count++;
        if (item.getPut() != null) count++;
        if (item.getDelete() != null) count++;
        if (item.getPatch() != null) count++;
        if (item.getHead() != null) count++;
        if (item.getOptions() != null) count++;
        if (item.getTrace() != null) count++;
        return count;
    }
}
