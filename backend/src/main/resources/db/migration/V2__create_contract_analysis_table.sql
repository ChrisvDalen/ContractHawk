CREATE TABLE contract_analysis (
    id                         BIGSERIAL PRIMARY KEY,
    contract_id                BIGINT NOT NULL REFERENCES contract (id) ON DELETE CASCADE,
    status                     VARCHAR(32) NOT NULL,
    started_at                 TIMESTAMP WITH TIME ZONE,
    finished_at                TIMESTAMP WITH TIME ZONE,
    valid_spec                 BOOLEAN,
    path_count                 INTEGER,
    operation_count            INTEGER,
    breaking_changes_detected  BOOLEAN,
    failure_reason             TEXT,
    summary                    JSONB,
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contract_analysis_contract_id ON contract_analysis (contract_id);
CREATE INDEX idx_contract_analysis_status ON contract_analysis (status);
CREATE INDEX idx_contract_analysis_contract_created ON contract_analysis (contract_id, created_at DESC);
