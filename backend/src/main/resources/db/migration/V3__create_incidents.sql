CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES services(id),
    title VARCHAR(200) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'INVESTIGATING',
    impact VARCHAR(30) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_incidents_status CHECK (
        status IN ('INVESTIGATING', 'IDENTIFIED', 'MONITORING', 'RESOLVED')
    ),
    CONSTRAINT chk_incidents_impact CHECK (
        impact IN ('MINOR', 'MAJOR', 'CRITICAL')
    )
);
