#!/bin/bash
# PostgreSQL Setup Script for Floggy
# This script creates the database, user, and schema for Floggy

set -e

echo "========================================="
echo "Floggy PostgreSQL Database Setup"
echo "========================================="

# Default configuration
DB_NAME="floggy"
DB_USER="floggy"
DB_PASSWORD="floggy123"
DB_HOST="localhost"
DB_PORT="5432"

# Check if PostgreSQL is running
if ! pg_isready -h $DB_HOST -p $DB_PORT > /dev/null 2>&1; then
    echo "PostgreSQL is not running on $DB_HOST:$DB_PORT"
    echo "Please start PostgreSQL or adjust connection settings"
    exit 1
fi

echo "Creating database '$DB_NAME' and user '$DB_USER'..."

# Connect to PostgreSQL and execute setup
PSQL_CMD="psql -h $DB_HOST -p $DB_PORT -U postgres"

# Create database if it doesn't exist
$PSQL_CMD -c "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1 || \
    $PSQL_CMD -c "CREATE DATABASE $DB_NAME;"

# Create user if it doesn't exist
$PSQL_CMD -c "SELECT 1 FROM pg_roles WHERE rolname = '$DB_USER'" | grep -q 1 || \
    $PSQL_CMD -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"

# Grant privileges
$PSQL_CMD -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"

echo "Database and user created successfully."

# Apply schema
echo "Applying schema to database '$DB_NAME'..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$(dirname "$0")/postgresql-schema.sql"

echo "========================================="
echo "Setup completed successfully!"
echo "========================================="
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo "Host: $DB_HOST:$DB_PORT"
echo ""
echo "Connection URL: postgresql://$DB_USER:$DB_PASSWORD@$DB_HOST:$DB_PORT/$DB_NAME"
echo ""
echo "You can now start the Floggy application."
echo "========================================="