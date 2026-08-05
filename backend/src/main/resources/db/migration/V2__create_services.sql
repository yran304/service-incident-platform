CREATE TABLE services (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    current_status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_services_organization_slug UNIQUE (organization_id, slug)
    /* this constraint enforces the rule that:
    Within the same organization, no two services can have the same slug. 
    Different organizations may use the same service slug.
    */
);