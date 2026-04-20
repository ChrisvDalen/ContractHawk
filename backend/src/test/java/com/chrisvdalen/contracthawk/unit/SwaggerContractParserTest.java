package com.chrisvdalen.contracthawk.unit;

import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;
import com.chrisvdalen.contracthawk.analysis.infrastructure.SwaggerContractParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerContractParserTest {

    private final SwaggerContractParser parser = new SwaggerContractParser();

    @Test
    void parsesValidSpecAndCountsPathsAndOperations() throws Exception {
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

        ParsedContract parsed = parser.parse(new ByteArrayInputStream(spec.getBytes(StandardCharsets.UTF_8)));

        assertThat(parsed.valid()).isTrue();
        assertThat(parsed.pathCount()).isEqualTo(2);
        assertThat(parsed.operationCount()).isEqualTo(3);
    }

    @Test
    void returnsInvalidForGarbage() throws Exception {
        ParsedContract parsed = parser.parse(
                new ByteArrayInputStream("not an openapi document".getBytes(StandardCharsets.UTF_8)));

        assertThat(parsed.valid()).isFalse();
        assertThat(parsed.pathCount()).isZero();
        assertThat(parsed.operationCount()).isZero();
    }
}
