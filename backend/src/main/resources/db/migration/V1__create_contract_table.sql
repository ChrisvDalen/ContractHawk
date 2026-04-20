CREATE TABLE contract (
    id                 BIGSERIAL PRIMARY KEY,
    service_name       VARCHAR(255) NOT NULL,
    version            VARCHAR(100) NOT NULL,
    original_filename  VARCHAR(512) NOT NULL,
    storage_path       VARCHAR(1024) NOT NULL,
    uploaded_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contract_service_name ON contract (service_name);
CREATE INDEX idx_contract_service_uploaded ON contract (service_name, uploaded_at DESC);
