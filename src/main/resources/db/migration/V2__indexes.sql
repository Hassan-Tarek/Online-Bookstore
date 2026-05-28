-- ==============================
-- V2__indexes.sql
-- ==============================

CREATE UNIQUE INDEX uq_active_users ON users(email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_active_reviews ON reviews(book_id, user_id) WHERE deleted_at IS NULL;

-- ==============================
-- USERS & ADDRESSES
-- ==============================
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_author_followers_user_id ON author_followers(user_id);

-- ==============================
-- SEARCHING
-- ==============================
-- Enable trigram for fast name search
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_books_title_trgm ON books USING gin (title gin_trgm_ops);

CREATE INDEX idx_books_price ON books(price);
CREATE INDEX idx_books_publication_date ON books(publication_date);
CREATE INDEX idx_books_series_id ON books(series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);
CREATE INDEX idx_book_authors_author_id ON book_authors(author_id);

-- ==============================
-- INVENTORY
-- ==============================
CREATE INDEX idx_inventories_book_id ON inventories(book_id);

-- ==============================
-- ORDERS & CARTS
-- ==============================
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

-- ==============================
-- PAYMENTS & SHIPMENTS
-- ==============================
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_shipments_order_id ON shipments(order_id);

-- ==============================
-- REVIEWS & NOTIFICATIONS
-- ==============================
CREATE INDEX idx_reviews_book_created ON reviews(book_id, created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id)
    WHERE status = 'UNREAD';
