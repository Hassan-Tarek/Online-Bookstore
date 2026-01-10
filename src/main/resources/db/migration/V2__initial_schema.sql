-- ==============================
-- V2__initial_schema.sql
-- ==============================

-- ==============================
-- USERS
-- ==============================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

-- ==============================
-- ADDRESSES
-- ==============================
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_addresses_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- BOOKS & CATEGORIES
-- ==============================
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    print_length INTEGER NOT NULL CHECK (print_length > 0),
    language VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    publication_year INTEGER NOT NULL,
    cover_image_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE book_categories (
    book_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    CONSTRAINT pk_book_category PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_categories_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_categories_category_id
        FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ==============================
-- INVENTORIES
-- ==============================
CREATE TABLE inventories (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL UNIQUE,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_inventory CHECK (reserved <= quantity),
    CONSTRAINT fk_inventories_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- SHOPPING_CARTS & CART_ITEMS
-- ==============================
CREATE TABLE shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,  -- one active cart per user
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_shopping_carts_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
    cart_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT pk_cart_item PRIMARY KEY (cart_id, book_id),
    CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES shopping_carts(id),
    CONSTRAINT fk_cart_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- ORDERS & ORDER_ITEMS
-- ==============================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    billing_address_id BIGINT NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL CHECK (total_price >= 0),
    status order_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_billing_address_id
        FOREIGN KEY (billing_address_id) REFERENCES addresses(id)
);

CREATE TABLE order_items (
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    CONSTRAINT pk_order_item PRIMARY KEY (order_id, book_id),
    CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- PAYMENTS
-- ==============================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_intent_id VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,   -- cents
    currency CHAR(3) DEFAULT 'USD',
    status payment_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_payments_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ==============================
-- SHIPMENTS
-- ==============================
CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    carrier VARCHAR(100),
    tracking_number VARCHAR(255) UNIQUE,
    status shipment_status NOT NULL DEFAULT 'PENDING',
    shipped_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    delivered_at TIMESTAMPTZ,
    CONSTRAINT fk_shipments_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ==============================
-- PROMOTIONS
-- ==============================
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type promotion_type NOT NULL,
    value NUMERIC(10, 2) NOT NULL,
    usage_limit INTEGER NOT NULL,
    used_count INTEGER NOT NULL DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_promotions_dates CHECK (end_date > start_date)
);

CREATE TABLE book_promotions (
    book_id BIGINT NOT NULL,
    promotion_id BIGINT NOT NULL,
    CONSTRAINT pk_book_promotion PRIMARY KEY (book_id, promotion_id),
    CONSTRAINT fk_book_promotions_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_promotions_promotion_id
        FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

-- ==============================
-- REVIEWS
-- ==============================
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_review UNIQUE (book_id, user_id),  -- one review per user
    CONSTRAINT fk_reviews_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_reviews_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- WISHLIST_ITEMS
-- ==============================
CREATE TABLE wishlist_items (
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_wishlist_item PRIMARY KEY (user_id, book_id),
    CONSTRAINT fk_wishlist_items_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wishlist_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- NOTIFICATIONS
-- ==============================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);
