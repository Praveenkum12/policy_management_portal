CREATE TABLE IF NOT EXISTS policies (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    policy_number  VARCHAR(50)    NOT NULL,
    customer_name  VARCHAR(120)   NOT NULL,
    policy_type    VARCHAR(20)    NOT NULL,
    premium_amount DECIMAL(12, 2) NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    start_date     DATE           NOT NULL,
    created_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_policies PRIMARY KEY (id),
    CONSTRAINT uk_policies_policy_number UNIQUE (policy_number),
    CONSTRAINT ck_policies_policy_type CHECK (policy_type IN ('LIFE', 'HEALTH', 'MOTOR', 'TRAVEL')),
    CONSTRAINT ck_policies_status CHECK (status IN ('ACTIVE', 'PENDING', 'CANCELLED')),
    CONSTRAINT ck_policies_premium CHECK (premium_amount > 0),

    INDEX idx_policies_status_created (status, created_at),
    INDEX idx_policies_customer_name (customer_name),
    INDEX idx_policies_start_date (start_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
