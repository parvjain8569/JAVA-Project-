CREATE DATABASE IF NOT EXISTS apexbank;
USE apexbank;

CREATE TABLE IF NOT EXISTS accounts (
    account_no VARCHAR(20) PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    pin VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(14,2) NOT NULL,
    overdraft_limit DECIMAL(14,2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT PRIMARY KEY,
    account_no VARCHAR(20) NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    remaining_balance DECIMAL(14,2) NOT NULL,
    remark VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
