CREATE TABLE incident_updates (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL REFERENCES incidents(id),
    message TEXT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ,
    CONSTRAINT chk_incident_updates_published_at CHECK (
        (is_published = FALSE AND published_at IS NULL)
        OR (is_published = TRUE AND published_at IS NOT NULL)
    )
);
