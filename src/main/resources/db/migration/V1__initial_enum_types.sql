-- ==============================
-- V1__initial_enum_types.sql
-- ==============================

CREATE TYPE role_type AS ENUM ('ADMIN', 'CUSTOMER');

CREATE TYPE promotion_type AS ENUM ('PERCENTAGE', 'FIXED');

CREATE TYPE inventory_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE order_status AS ENUM ('CREATED', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'REFUNDED');

CREATE TYPE shipment_status AS ENUM ('PENDING', 'SHIPPED', 'DELIVERED', 'RETURNED');

CREATE TYPE notification_status AS ENUM ('UNREAD', 'READ');
