package com.chrisvdalen.contracthawk.analysis.application;

import com.chrisvdalen.contracthawk.analysis.domain.ParsedContract;

import java.io.IOException;
import java.io.InputStream;

public interface ContractParser {

    ParsedContract parse(InputStream content) throws IOException;
}
