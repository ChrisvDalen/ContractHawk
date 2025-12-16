CREATE TABLE api_sync_run (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_contract(id) ON DELETE CASCADE,
    run_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    added_count INTEGER NOT NULL DEFAULT 0,
    updated_count INTEGER NOT NULL DEFAULT 0,
    deleted_count INTEGER NOT NULL DEFAULT 0,
    breaks_detected BOOLEAN NOT NULL DEFAULT FALSE,
    error_message VARCHAR(500)
);

CREATE INDEX idx_api_sync_run_api_id ON api_sync_run(api_id);
CREATE INDEX idx_api_sync_run_run_at ON api_sync_run(api_id, run_at DESC);

CREATE TABLE api_breaking_change (
    id UUID PRIMARY KEY,
    sync_run_id UUID NOT NULL REFERENCES api_sync_run(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(200) NOT NULL,
    details VARCHAR(300)
);

CREATE INDEX idx_api_breaking_change_sync_run_id ON api_breaking_change(sync_run_id);

