-- ==============================
-- V3__add_indexes.sql
-- ==============================

-- ==============================
-- USERS
-- ==============================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- ==============================
-- ADDRESSES
-- ==============================
CREATE INDEX idx_addresses_user_id ON addresses(user_id);

-- ==============================
-- BOOKS
-- ==============================
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_price ON books(price);

-- ==============================
-- BOOK CATEGORIES
-- ==============================
CREATE INDEX idx_book_categories_book_id ON book_categories(book_id);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);

-- ==============================
-- INVENTORIES
-- ==============================
CREATE UNIQUE INDEX idx_inventories_book_id ON inventories(book_id);

-- ==============================
-- SHOPPING CARTS & CART ITEMS
-- ==============================
CREATE INDEX idx_shopping_carts_user_id ON shopping_carts(user_id);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_book_id ON cart_items(book_id);

-- ==============================
-- ORDERS
-- ==============================
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- ==============================
-- ORDER ITEMS
-- ==============================
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_book_id ON order_items(book_id);

-- ==============================
-- PAYMENTS
-- ==============================
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_payment_intent_id ON payments(payment_intent_id);
CREATE INDEX idx_payments_status ON payments(status);

-- ==============================
-- SHIPMENTS
-- ==============================
CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_status ON shipments(status);

-- ==============================
-- PROMOTIONS
-- ==============================
CREATE INDEX idx_promotions_code ON promotions(code);
CREATE INDEX idx_promotions_active_dates
    ON promotions(active, start_date, end_date);

-- ==============================
-- REVIEWS
-- ==============================
CREATE INDEX idx_reviews_book_id ON reviews(book_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- ==============================
-- WISHLIST
-- ==============================
CREATE INDEX idx_wishlist_items_user_id ON wishlist_items(user_id);
CREATE INDEX idx_wishlist_items_book_id ON wishlist_items(book_id);

-- ==============================
-- NOTIFICATIONS
-- ==============================
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
