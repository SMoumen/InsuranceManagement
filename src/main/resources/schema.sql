-- Insurance Management System Database Schema
-- PostgreSQL

-- Clients Table
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    client_type VARCHAR(31) NOT NULL CHECK (client_type IN ('PERSON', 'COMPANY')),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    birthdate DATE,
    company_identifier VARCHAR(10) UNIQUE,
    CONSTRAINT check_person_birthdate CHECK (
        (client_type = 'PERSON' AND birthdate IS NOT NULL) OR
        (client_type = 'COMPANY' AND birthdate IS NULL)
    ),
    CONSTRAINT check_company_identifier CHECK (
        (client_type = 'COMPANY' AND company_identifier IS NOT NULL) OR
        (client_type = 'PERSON' AND company_identifier IS NULL)
    )
);

-- Contracts Table
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    cost_amount DECIMAL(19, 2) NOT NULL CHECK (cost_amount > 0),
    update_date DATE NOT NULL,
    CONSTRAINT fk_contract_client FOREIGN KEY (client_id)
        REFERENCES clients(id) ON DELETE CASCADE
);

-- Indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_client_type ON clients(client_type);
CREATE INDEX IF NOT EXISTS idx_client_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_company_identifier ON clients(company_identifier);

CREATE INDEX IF NOT EXISTS idx_contract_client_id ON contracts(client_id);
CREATE INDEX IF NOT EXISTS idx_contract_end_date ON contracts(end_date);
CREATE INDEX IF NOT EXISTS idx_contract_update_date ON contracts(update_date);
CREATE INDEX IF NOT EXISTS idx_contract_active ON contracts(client_id, end_date);

-- Comments for documentation (safe to re-run)
COMMENT ON TABLE clients IS 'Stores insurance clients - both persons and companies';
COMMENT ON TABLE contracts IS 'Stores insurance contracts associated with clients';

COMMENT ON COLUMN clients.client_type IS 'Discriminator: PERSON or COMPANY';
COMMENT ON COLUMN clients.birthdate IS 'Required for PERSON, null for COMPANY (immutable)';
COMMENT ON COLUMN clients.company_identifier IS 'Required for COMPANY, null for PERSON (immutable, format: aaa-123)';

COMMENT ON COLUMN contracts.update_date IS 'Internal field - tracks last modification date, not exposed via API';
COMMENT ON COLUMN contracts.end_date IS 'NULL means indefinite contract';

-- Sample data (optional - for development/testing; idempotent inserts)
-- INSERT INTO clients (client_type, name, email, phone, birthdate)
-- VALUES ('PERSON', 'John Doe', 'john.doe@example.com', '+33612345678', '1990-05-15')
-- ON CONFLICT (email) DO NOTHING;

-- INSERT INTO clients (client_type, name, email, phone, company_identifier)
-- VALUES ('COMPANY', 'Tech Corp', 'contact@techcorp.com', '+33698765432', 'abc-123')
-- ON CONFLICT (company_identifier) DO NOTHING;

-- Grant permissions (adjust based on your database user; safe to re-run)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO insurance_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO insurance_user;