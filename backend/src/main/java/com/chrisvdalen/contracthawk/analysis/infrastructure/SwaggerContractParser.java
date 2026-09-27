package com.chrisvdalen.contracthawk.analysis.infrastructure;

import com.chrisvdalen.contracthawk.analysis.application.ContractParser;
import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class SwaggerContractParser implements ContractParser {

    private static final Map<PathItem.HttpMethod, String> METHOD_NAMES = new EnumMap<>(PathItem.HttpMethod.class);

    static {
        METHOD_NAMES.put(PathItem.HttpMethod.GET, "get");
        METHOD_NAMES.put(PathItem.HttpMethod.POST, "post");
        METHOD_NAMES.put(PathItem.HttpMethod.PUT, "put");
        METHOD_NAMES.put(PathItem.HttpMethod.DELETE, "delete");
        METHOD_NAMES.put(PathItem.HttpMethod.PATCH, "patch");
        METHOD_NAMES.put(PathItem.HttpMethod.HEAD, "head");
        METHOD_NAMES.put(PathItem.HttpMethod.OPTIONS, "options");
        METHOD_NAMES.put(PathItem.HttpMethod.TRACE, "trace");
    }

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
            return new ParsedContract(false, 0, 0, List.of("Parse error: " + e.getMessage()), Map.of());
        }

        List<String> messages = Optional.ofNullable(result.getMessages()).orElse(List.of());
        OpenAPI openApi = result.getOpenAPI();

        if (openApi == null || openApi.getPaths() == null) {
            return new ParsedContract(false, 0, 0, messages, Map.of());
        }

        Map<String, Set<String>> paths = new TreeMap<>();
        int operationCount = 0;
        for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
            Set<String> methods = new TreeSet<>();
            for (Map.Entry<PathItem.HttpMethod, Operation> op : entry.getValue().readOperationsMap().entrySet()) {
                String name = METHOD_NAMES.get(op.getKey());
                if (name != null) {
                    methods.add(name);
                    operationCount++;
                }
            }
            paths.put(entry.getKey(), methods);
        }

        boolean valid = messages.isEmpty();
        return new ParsedContract(valid, paths.size(), operationCount, messages, paths);
    }
}
