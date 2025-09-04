-- Crea esquema para microservicio de credit requests
CREATE SCHEMA IF NOT EXISTS credit;

-- Habilita la extensión UUID para generar UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de tipos de préstamo
CREATE TABLE IF NOT EXISTS credit.loan_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    minimum_amount DECIMAL(15,2) NOT NULL CHECK (minimum_amount >= 0),
    maximum_amount DECIMAL(15,2) NOT NULL CHECK (maximum_amount >= 0),
    interest_rate DECIMAL(5,2) NOT NULL CHECK (interest_rate >= 0 AND interest_rate <= 100),
    automatic_validation BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Tabla de estados de solicitud
CREATE TABLE IF NOT EXISTS credit.request_states (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Tabla de solicitudes de crédito
CREATE TABLE IF NOT EXISTS credit.credit_applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    month_term INTEGER NOT NULL CHECK (month_term > 0 AND month_term <= 120),
    email VARCHAR(255) NOT NULL,
    document_number VARCHAR(50) NOT NULL,
    loan_type_id INTEGER NOT NULL,
    request_state_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_credit_applications_loan_type FOREIGN KEY (loan_type_id) REFERENCES credit.loan_types(id),
    CONSTRAINT fk_credit_applications_request_state FOREIGN KEY (request_state_id) REFERENCES credit.request_states(id)
);

-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_credit_applications_email ON credit.credit_applications(email);
CREATE INDEX IF NOT EXISTS idx_credit_applications_document_number ON credit.credit_applications(document_number);
CREATE INDEX IF NOT EXISTS idx_credit_applications_loan_type_id ON credit.credit_applications(loan_type_id);
CREATE INDEX IF NOT EXISTS idx_credit_applications_request_state_id ON credit.credit_applications(request_state_id);

-- Datos de ejemplo para tipos de préstamo
INSERT INTO credit.loan_types (name, minimum_amount, maximum_amount, interest_rate, automatic_validation)
VALUES 
    ('PERSONAL', 1000000.00, 50000000.00, 2.5, FALSE),
    ('VEHICULAR', 5000000.00, 100000000.00, 1.8, TRUE),
    ('HIPOTECARIO', 20000000.00, 500000000.00, 1.2, FALSE),
    ('MICROCREDITO', 100000.00, 2000000.00, 3.5, TRUE)
ON CONFLICT (name) DO NOTHING;

-- Datos de ejemplo para estados de solicitud
INSERT INTO credit.request_states (name, description)
VALUES 
    ('PENDIENTE', 'Solicitud en revisión'),
    ('APROBADA', 'Solicitud aprobada'),
    ('RECHAZADA', 'Solicitud rechazada'),
    ('EN_ANALISIS', 'Solicitud en análisis de riesgo'),
    ('CANCELADA', 'Solicitud cancelada por el usuario')
ON CONFLICT (name) DO NOTHING;

-- Datos de ejemplo para solicitudes de crédito
INSERT INTO credit.credit_applications (amount, month_term, email, document_number, loan_type_id, request_state_id)
VALUES 
    (5000000.00, 24, 'usuario1@example.com', '12345678', 1, 1),
    (15000000.00, 36, 'usuario2@example.com', '87654321', 2, 2),
    (8000000.00, 18, 'usuario3@example.com', '11223344', 1, 3)
ON CONFLICT DO NOTHING;

-- Comentarios de las tablas
COMMENT ON TABLE credit.loan_types IS 'Tipos de préstamo disponibles en el sistema';
COMMENT ON TABLE credit.request_states IS 'Estados posibles de una solicitud de crédito';
COMMENT ON TABLE credit.credit_applications IS 'Solicitudes de crédito de los usuarios';

-- Comentarios de las columnas principales
COMMENT ON COLUMN credit.credit_applications.amount IS 'Monto solicitado en pesos colombianos';
COMMENT ON COLUMN credit.credit_applications.month_term IS 'Plazo del préstamo en meses';
COMMENT ON COLUMN credit.credit_applications.email IS 'Email del solicitante';
COMMENT ON COLUMN credit.credit_applications.document_number IS 'Número de documento del solicitante';
