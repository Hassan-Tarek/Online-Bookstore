-- ==============================
-- V1__initial_enum_types.sql
-- ==============================

CREATE TYPE user_role AS ENUM ('ADMIN', 'CUSTOMER');

CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'CANCELLED', 'SHIPPED', 'DELIVERED');

CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED');

CREATE TYPE shipment_status AS ENUM ('PENDING', 'SHIPPED', 'DELIVERED');

CREATE TYPE promotion_type AS ENUM ('PERCENTAGE', 'FIXED');
