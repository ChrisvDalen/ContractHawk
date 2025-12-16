CREATE TABLE api_contract (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    base_url VARCHAR(200) NOT NULL,
    version VARCHAR(40) NOT NULL,
    owner_team VARCHAR(60) NOT NULL,
    lifecycle VARCHAR(20) NOT NULL,
    open_api_url VARCHAR(300),
    description VARCHAR(400),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_name_per_team UNIQUE (name, owner_team)
);

CREATE INDEX idx_api_contract_name ON api_contract(name);
CREATE INDEX idx_api_contract_owner_team ON api_contract(owner_team);
CREATE INDEX idx_api_contract_lifecycle ON api_contract(lifecycle);
CREATE INDEX idx_api_contract_updated_at ON api_contract(updated_at);

CREATE TABLE endpoint (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_contract(id) ON DELETE CASCADE,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(200) NOT NULL,
    description VARCHAR(300),
    deprecated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_endpoint_per_api UNIQUE (api_id, method, path)
);

CREATE INDEX idx_endpoint_api_id ON endpoint(api_id);

CREATE TABLE changelog_entry (
    id UUID PRIMARY KEY,
    api_id UUID NOT NULL REFERENCES api_contract(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    breaking BOOLEAN NOT NULL DEFAULT FALSE,
    summary VARCHAR(200) NOT NULL,
    details VARCHAR(1000),
    released_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_changelog_entry_api_id ON changelog_entry(api_id);
CREATE INDEX idx_changelog_entry_released_at ON changelog_entry(api_id, released_at DESC);

