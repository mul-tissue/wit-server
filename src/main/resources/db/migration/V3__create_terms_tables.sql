-- V3__create_terms_tables.sql
-- Description: Create terms and user_terms_agreements tables

-- Terms table
CREATE TABLE terms (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    public_id VARCHAR(26) NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    version VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_terms_public_id UNIQUE (public_id)
);

CREATE INDEX idx_terms_public_id ON terms(public_id);
CREATE INDEX idx_terms_type ON terms(type);
CREATE INDEX idx_terms_active ON terms(active);

-- User terms agreements table
CREATE TABLE user_terms_agreements (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    terms_id BIGINT NOT NULL,
    agreed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_terms_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_terms_terms FOREIGN KEY (terms_id) REFERENCES terms(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_terms UNIQUE (user_id, terms_id)
);

CREATE INDEX idx_user_terms_user_id ON user_terms_agreements(user_id);
CREATE INDEX idx_user_terms_terms_id ON user_terms_agreements(terms_id);

-- Insert default terms
INSERT INTO terms (public_id, type, title, content, version, required, active) VALUES
('01JKTERMS001SERVICE00001', 'SERVICE', '서비스 이용약관', '서비스 이용약관 내용입니다. 추후 실제 약관 내용으로 교체해주세요.', '1.0', TRUE, TRUE),
('01JKTERMS002PRIVACY00001', 'PRIVACY', '개인정보 처리방침', '개인정보 처리방침 내용입니다. 추후 실제 약관 내용으로 교체해주세요.', '1.0', TRUE, TRUE),
('01JKTERMS003MARKETNG0001', 'MARKETING', '마케팅 정보 수신 동의', '마케팅 정보 수신 동의 내용입니다. 추후 실제 약관 내용으로 교체해주세요.', '1.0', FALSE, TRUE);
