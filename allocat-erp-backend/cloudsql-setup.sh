#!/bin/bash

# Cloud SQL Setup Script for Allocat ERP
# Usage: ./cloudsql-setup.sh [project-id] [db-password]

set -e

PROJECT_ID=${1:-${GOOGLE_CLOUD_PROJECT}}
DB_PASSWORD=${2}
REGION="northamerica-northeast1"
INSTANCE_NAME="allocat-db"
DATABASE_NAME="allocat_db"
DB_USER="allocat_user"

if [ -z "$PROJECT_ID" ]; then
    echo "Error: Project ID is required"
    echo "Usage: ./cloudsql-setup.sh [project-id] [db-password]"
    exit 1
fi

if [ -z "$DB_PASSWORD" ]; then
    echo "Error: Database password is required"
    echo "Usage: ./cloudsql-setup.sh [project-id] [db-password]"
    exit 1
fi

echo "🗄️  Setting up Cloud SQL for Allocat ERP..."
echo "Project ID: $PROJECT_ID"
echo "Region: $REGION"
echo "Instance: $INSTANCE_NAME"

# Set the project
gcloud config set project $PROJECT_ID

# Enable SQL Admin API
echo "📦 Enabling SQL Admin API..."
gcloud services enable sqladmin.googleapis.com

# Check if instance already exists
if gcloud sql instances describe $INSTANCE_NAME --project=$PROJECT_ID &>/dev/null; then
    echo "⚠️  Instance $INSTANCE_NAME already exists. Skipping creation."
else
    # Create Cloud SQL instance
    echo "🔨 Creating Cloud SQL instance..."
    gcloud sql instances create $INSTANCE_NAME \
        --database-version=POSTGRES_15 \
        --tier=db-f1-micro \
        --region=$REGION \
        --root-password=$DB_PASSWORD \
        --storage-type=SSD \
        --storage-size=20GB \
        --backup-start-time=03:00 \
        --enable-bin-log \
        --project=$PROJECT_ID
    
    echo "✅ Instance created successfully"
fi

# Create database
echo "📊 Creating database..."
if gcloud sql databases describe $DATABASE_NAME --instance=$INSTANCE_NAME --project=$PROJECT_ID &>/dev/null; then
    echo "⚠️  Database $DATABASE_NAME already exists. Skipping creation."
else
    gcloud sql databases create $DATABASE_NAME \
        --instance=$INSTANCE_NAME \
        --project=$PROJECT_ID
    echo "✅ Database created successfully"
fi

# Create database user
echo "👤 Creating database user..."
if gcloud sql users describe $DB_USER --instance=$INSTANCE_NAME --project=$PROJECT_ID &>/dev/null; then
    echo "⚠️  User $DB_USER already exists. Updating password..."
    gcloud sql users set-password $DB_USER \
        --instance=$INSTANCE_NAME \
        --password=$DB_PASSWORD \
        --project=$PROJECT_ID
else
    gcloud sql users create $DB_USER \
        --instance=$INSTANCE_NAME \
        --password=$DB_PASSWORD \
        --project=$PROJECT_ID
fi
echo "✅ User created/updated successfully"

# Get connection name
CONNECTION_NAME=$(gcloud sql instances describe $INSTANCE_NAME \
    --format="value(connectionName)" \
    --project=$PROJECT_ID)

echo ""
echo "✅ Cloud SQL setup complete!"
echo ""
echo "Connection details:"
echo "  Instance: $INSTANCE_NAME"
echo "  Database: $DATABASE_NAME"
echo "  User: $DB_USER"
echo "  Connection Name: $CONNECTION_NAME"
echo ""
echo "Next steps:"
echo "1. Store the password in Secret Manager:"
echo "   echo -n '$DB_PASSWORD' | gcloud secrets create db-password --data-file=-"
echo ""
echo "2. Store the connection name in Secret Manager:"
echo "   echo -n '$CONNECTION_NAME' | gcloud secrets create cloudsql-connection-name --data-file=-"
echo ""
echo "3. Update your Cloud Run service to use this connection"
