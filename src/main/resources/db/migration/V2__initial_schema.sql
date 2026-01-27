-- ==============================
-- V2__initial_schema.sql
-- ==============================

-- ==============================
-- USERS
-- ==============================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role role_type NOT NULL DEFAULT 'CUSTOMER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    biography TEXT,
    profile_image_url VARCHAR(500),
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
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
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
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    publication_date DATE NOT NULL,
    cover_image_url VARCHAR(500),
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
    name VARCHAR(100) NOT NULL UNIQUE
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
-- PROMOTIONS
-- ==============================
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type promotion_type NOT NULL,
    value NUMERIC(10, 2) NOT NULL,
    usage_limit INTEGER NOT NULL,
    used_count INTEGER NOT NULL DEFAULT 0,
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
            (type = 'FIXED' AND value > 0)
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
-- REVIEWS
-- ==============================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL,
    user_id UUID NOT NULL,
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
-- INVENTORIES
-- ==============================
CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    book_id UUID NOT NULL UNIQUE,
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved INTEGER NOT NULL DEFAULT 0,
    status inventory_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT chk_inventory CHECK (reserved <= quantity),
    CONSTRAINT fk_inventories_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- SHOPPING_CARTS & CART_ITEMS
-- ==============================
CREATE TABLE shopping_carts (
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
    CONSTRAINT uq_cart_item UNIQUE (cart_id, book_id),
    CONSTRAINT fk_cart_items_cart_id
        FOREIGN KEY (cart_id) REFERENCES shopping_carts(id),
    CONSTRAINT fk_cart_items_book_id
        FOREIGN KEY (book_id) REFERENCES books(id)
);

-- ==============================
-- ORDERS & ORDER_ITEMS
-- ==============================
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    billing_address_id UUID NOT NULL,
    shipping_address_id UUID NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL CHECK (total_price >= 0),
    status order_status NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_orders_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_billing_address_id
        FOREIGN KEY (billing_address_id) REFERENCES addresses(id),
    CONSTRAINT fk_orders_shipping_address_id
        FOREIGN KEY (shipping_address_id) REFERENCES addresses(id)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    book_id UUID NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
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
    order_id UUID NOT NULL,
    payment_intent_id VARCHAR(255) NOT NULL,
    amount BIGINT NOT NULL,   -- cents
    currency CHAR(3) DEFAULT 'USD' CHECK (currency ~ '^[A-Z]{3}$'),
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
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    carrier VARCHAR(100),
    tracking_number VARCHAR(255) UNIQUE,
    status shipment_status NOT NULL DEFAULT 'PENDING',
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
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
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
    status notification_status NOT NULL DEFAULT 'UNREAD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ,
    CONSTRAINT fk_notifications_user_id
        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==============================
-- REPORTS
-- ==============================
CREATE TABLE daily_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date DATE NOT NULL UNIQUE,
    total_revenue NUMERIC(10, 2) NOT NULL DEFAULT 0.0,
    orders_count INTEGER NOT NULL DEFAULT 0,
    new_users_count INTEGER NOT NULL DEFAULT 0,
    top_selling_books TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
