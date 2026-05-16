-- V1__initial_schema.sql
-- PostgreSQL schema for Academix Coaching Center Management System

CREATE TABLE branch (
    id              SERIAL PRIMARY KEY,
    branch_code     VARCHAR(20)  NOT NULL UNIQUE,
    branch_name     VARCHAR(100) NOT NULL,
    address         VARCHAR(255),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    manager_name    VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    opened_date     DATE
);

CREATE TABLE app_user (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    full_name       VARCHAR(100),
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE user_branch_role (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER      NOT NULL REFERENCES app_user(id),
    branch_id       INTEGER      REFERENCES branch(id),
    role            VARCHAR(30)  NOT NULL,
    UNIQUE(user_id, branch_id, role)
);

CREATE TABLE teacher (
    id              SERIAL PRIMARY KEY,
    teacher_code    VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    subject         VARCHAR(100),
    qualification   VARCHAR(200),
    base_salary     DECIMAL(12,2) NOT NULL DEFAULT 0,
    joining_date    DATE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

CREATE TABLE batch (
    id              SERIAL PRIMARY KEY,
    batch_code      VARCHAR(20)  NOT NULL,
    batch_name      VARCHAR(100) NOT NULL,
    subject         VARCHAR(100),
    schedule        VARCHAR(200),
    capacity        INTEGER,
    monthly_fee     DECIMAL(10,2) NOT NULL DEFAULT 0,
    teacher_id      INTEGER      REFERENCES teacher(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE,
    end_date        DATE,
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

CREATE TABLE student (
    id              SERIAL PRIMARY KEY,
    student_code    VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    guardian_name   VARCHAR(100),
    guardian_phone  VARCHAR(20),
    address         VARCHAR(255),
    date_of_birth   DATE,
    batch_id        INTEGER      REFERENCES batch(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    admission_date  DATE,
    photo_path      VARCHAR(255),
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

CREATE TABLE admission (
    id              SERIAL PRIMARY KEY,
    admission_number VARCHAR(30) NOT NULL UNIQUE,
    student_id      INTEGER      NOT NULL REFERENCES student(id),
    batch_id        INTEGER      NOT NULL REFERENCES batch(id),
    branch_id       INTEGER      NOT NULL REFERENCES branch(id),
    admission_date  DATE         NOT NULL,
    admission_fee   DECIMAL(10,2),
    remarks         VARCHAR(500)
);

CREATE TABLE fee_payment (
    id              SERIAL PRIMARY KEY,
    receipt_number  VARCHAR(30)  NOT NULL UNIQUE,
    student_id      INTEGER      NOT NULL REFERENCES student(id),
    batch_id        INTEGER      REFERENCES batch(id),
    month           SMALLINT,
    year            SMALLINT,
    amount          DECIMAL(10,2) NOT NULL,
    payment_mode    VARCHAR(20)  NOT NULL DEFAULT 'CASH',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    paid_date       DATE,
    notes           VARCHAR(500),
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

CREATE TABLE payroll_entry (
    id              SERIAL PRIMARY KEY,
    teacher_id      INTEGER      NOT NULL REFERENCES teacher(id),
    month           SMALLINT     NOT NULL,
    year            SMALLINT     NOT NULL,
    base_salary     DECIMAL(12,2) NOT NULL,
    bonus           DECIMAL(12,2) NOT NULL DEFAULT 0,
    deduction       DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_pay         DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    disbursed_date  DATE,
    remarks         VARCHAR(500),
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

CREATE TABLE expenditure (
    id              SERIAL PRIMARY KEY,
    description     VARCHAR(255) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    category        VARCHAR(50)  NOT NULL,
    date            DATE         NOT NULL,
    voucher_number  VARCHAR(50),
    paid_to         VARCHAR(100),
    approved_by     VARCHAR(100),
    branch_id       INTEGER      NOT NULL REFERENCES branch(id)
);

-- Indexes for common queries
CREATE INDEX idx_student_branch ON student(branch_id);
CREATE INDEX idx_student_batch ON student(batch_id);
CREATE INDEX idx_batch_branch ON batch(branch_id);
CREATE INDEX idx_teacher_branch ON teacher(branch_id);
CREATE INDEX idx_fee_branch ON fee_payment(branch_id);
CREATE INDEX idx_fee_status ON fee_payment(status);
CREATE INDEX idx_payroll_branch ON payroll_entry(branch_id);
CREATE INDEX idx_expenditure_branch ON expenditure(branch_id);
