-- ==============================
-- V1__initial_schema.sql
-- ==============================

-- ==============================
-- USERS
-- ==============================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    profile_image_url VARCHAR(500),
    profile_image_public_id VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_user_role
        CHECK (role IN ('ADMIN', 'CUSTOMER'))
);

CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    biography TEXT,
    profile_image_url VARCHAR(500),
    profile_image_public_id VARCHAR(500),
    followers_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE author_followers (
    author_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT pk_author_followers PRIMARY KEY (author_id, user_id),
    CONSTRAINT fk_author_followers_author_id
        FOREIGN KEY (author_id) REFERENCES authors(id),
    CONSTRAINT fk_author_followers_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- ADDRESSES
-- ==============================
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_addresses_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- SERIES
-- ==============================
CREATE TABLE series (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    cover_image_public_id VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

-- ==============================
-- BOOKS
-- ==============================
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    series_id UUID,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    print_length INTEGER NOT NULL CHECK (print_length > 0),
    language VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    publisher VARCHAR(100) NOT NULL,
    publication_date DATE NOT NULL,
    rating_count BIGINT NOT NULL DEFAULT 0,
    average_rating NUMERIC(12, 2) NOT NULL DEFAULT 0,
    cover_image_url VARCHAR(500),
    cover_image_public_id VARCHAR(500),
    series_order INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_books_series_id
        FOREIGN KEY (series_id) REFERENCES series(id),
    CONSTRAINT chk_series_consistency
        CHECK (
            (series_id IS NULL AND series_order IS NULL)
                OR
            (series_id IS NOT NULL AND series_order IS NOT NULL)
        )
);

CREATE TABLE book_authors (
    book_id UUID NOT NULL,
    author_id UUID NOT NULL,
    CONSTRAINT pk_book_authors PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_book_authors_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_authors_author_id
        FOREIGN KEY (author_id) REFERENCES authors(id)
);

-- ==============================
-- CATEGORIES
-- ==============================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE book_categories (
    book_id UUID NOT NULL,
    category_id UUID NOT NULL,
    CONSTRAINT pk_book_category PRIMARY KEY (book_id, category_id),
    CONSTRAINT fk_book_categories_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_categories_category_id
        FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ==============================
-- REVIEWS
-- ==============================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL,
    user_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(150),
    content TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_reviews_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_reviews_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- INVENTORIES
-- ==============================
CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL UNIQUE,
    available_stock INTEGER NOT NULL DEFAULT 0 CHECK (available_stock >= 0),
    reserved_stock INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'OUT_OF_STOCK' CHECK (status IN ('IN_STOCK', 'OUT_OF_STOCK')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_inventory
        CHECK (reserved_stock <= available_stock),
    CONSTRAINT fk_inventories_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- PROMOTIONS
-- ==============================
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('PERCENTAGE', 'FIXED')),
    scope VARCHAR(30) NOT NULL CHECK (scope IN ('GLOBAL', 'BOOK')),
    value NUMERIC(12, 2) NOT NULL,
    usage_limit INTEGER NOT NULL,
    used_count INTEGER NOT NULL DEFAULT 0,
    min_checkout_amount NUMERIC(12, 2) DEFAULT 0.0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_promotions_value
        CHECK (
            (type = 'PERCENTAGE' AND value > 0 AND value <= 100)
                OR
            (type = 'FIXED' AND value > 0 AND (
                scope != 'GLOBAL' OR value <= COALESCE(min_checkout_amount, 0.0)
                ))
            ),
    CONSTRAINT chk_promotions_usage CHECK (used_count <= usage_limit),
    CONSTRAINT chk_promotions_dates CHECK (end_date > start_date)
);

CREATE TABLE book_promotions (
    book_id UUID NOT NULL,
    promotion_id UUID NOT NULL,
    CONSTRAINT pk_book_promotion PRIMARY KEY (book_id, promotion_id),
    CONSTRAINT fk_book_promotions_book_id
        FOREIGN KEY (book_id) REFERENCES books(id),
    CONSTRAINT fk_book_promotions_promotion_id
        FOREIGN KEY (promotion_id) REFERENCES promotions(id)
);

-- ==============================
-- CARTS & CART_ITEMS
-- ==============================
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,  -- one active cart per user
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_shopping_carts_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    book_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_cart_item UNIQUE (cart_id, book_id),
    CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_cart_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- ORDERS & ORDER_ITEMS
-- ==============================
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    promo_code VARCHAR(50),
    billing_address JSONB NOT NULL,
    shipping_address JSONB NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    tax NUMERIC(12, 2) NOT NULL CHECK (tax >= 0),
    shipping_fee NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    total_price NUMERIC(12, 2) NOT NULL CHECK (total_price >= 0),
    shipping_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_promo_code
        FOREIGN KEY (promo_code) REFERENCES promotions(code)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    book_id UUID NOT NULL,
    original_unit_price NUMERIC(12, 2) NOT NULL CHECK (original_unit_price >= 0),
    final_unit_price NUMERIC(12, 2) NOT NULL CHECK (final_unit_price >= 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_order_item UNIQUE (order_id, book_id),
    CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- PAYMENTS
-- ==============================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL DEFAULT 'STRIPE',
    payment_intent_id VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD' CHECK (currency ~ '^[A-Z]{3}$'),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_payments_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ==============================
-- SHIPMENTS
-- ==============================
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    shipped_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_shipments_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ==============================
-- WISHLISTS & WISHLIST_ITEMS
-- ==============================
CREATE TABLE wishlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT fk_wishlists_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE wishlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wishlist_id UUID NOT NULL,
    book_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_wishlist_item UNIQUE (wishlist_id, book_id),
    CONSTRAINT fk_wishlist_items_wishlist_id
        FOREIGN KEY (wishlist_id) REFERENCES wishlists(id),
    CONSTRAINT fk_wishlist_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- NOTIFICATIONS
-- ==============================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNREAD',
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_notifications_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);
