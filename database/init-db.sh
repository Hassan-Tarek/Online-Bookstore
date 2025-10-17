#!/bin/bash
set -e

echo "📦 Initializing database setup..."

# Define the paths
TEMPLATE_SQL="/tmp/bookstore-setup-template.sql"
GENERATED_SQL="/tmp/bookstore-setup.sql"

# Load .env varibales into shell
echo "Loading .env variables into shell..."
export $(grep -v '^#' .env | xargs)

# Replace placeholders
echo "Replacing placeholders..."
sed -e "s/\${DB_NAME_DEV}/$DB_NAME_DEV/g" \
    -e "s/\${DB_USER_DEV}/$DB_USER_DEV/g" \
    -e "s/\${DB_PASSWORD_DEV}/$DB_PASSWORD_DEV/g" \
    -e "s/\${DB_NAME_TEST}/$DB_NAME_TEST/g" \
    -e "s/\${DB_USER_TEST}/$DB_USER_TEST/g" \
    -e "s/\${DB_PASSWORD_TEST}/$DB_PASSWORD_TEST/g" \
    -e "s/\${DB_NAME_PROD}/$DB_NAME_PROD/g" \
    -e "s/\${DB_USER_PROD}/$DB_USER_PROD/g" \
    -e "s/\${DB_PASSWORD_PROD}/$DB_PASSWORD_PROD/g" \
    $TEMPLATE_SQL > $GENERATED_SQL

echo "✅ Generated /docker-entrypoint-initdb.d/bookstore-setup.sql"
ls -l /tmp/

# Run the generated script
echo "🔑 Executing database setup script..."
mysql -u root -p${MYSQL_ROOT_PASSWORD} < "$GENERATED_SQL"

echo "🎯 Database init script prepared successfully."
