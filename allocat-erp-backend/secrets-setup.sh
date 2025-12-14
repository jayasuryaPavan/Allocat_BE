#!/bin/bash

# Secret Manager Setup Script for Allocat ERP
# Usage: ./secrets-setup.sh [project-id]

set -e

PROJECT_ID=${1:-${GOOGLE_CLOUD_PROJECT}}

if [ -z "$PROJECT_ID" ]; then
    echo "Error: Project ID is required"
    echo "Usage: ./secrets-setup.sh [project-id]"
    exit 1
fi

echo "🔐 Setting up secrets in Secret Manager..."
echo "Project ID: $PROJECT_ID"

# Set the project
gcloud config set project $PROJECT_ID

# Enable Secret Manager API
echo "📦 Enabling Secret Manager API..."
gcloud services enable secretmanager.googleapis.com

# Prompt for secrets
echo ""
echo "Please provide the following secrets:"
echo ""

# JWT Secret
read -sp "Enter JWT Secret (min 256 bits): " JWT_SECRET
echo ""
if [ -z "$JWT_SECRET" ]; then
    echo "⚠️  JWT Secret is empty. Using default (NOT RECOMMENDED FOR PRODUCTION)"
    JWT_SECRET="allocat-erp-super-secret-jwt-signing-key-minimum-256-bits-required-for-security"
fi

# Database Password
read -sp "Enter Database Password: " DB_PASSWORD
echo ""
if [ -z "$DB_PASSWORD" ]; then
    echo "❌ Database password is required!"
    exit 1
fi

# Database Username
read -p "Enter Database Username [allocat_user]: " DB_USER
DB_USER=${DB_USER:-allocat_user}

# Cloud SQL Connection Name
read -p "Enter Cloud SQL Connection Name [PROJECT_ID:northamerica-northeast1:allocat-db]: " CONNECTION_NAME
if [ -z "$CONNECTION_NAME" ]; then
    CONNECTION_NAME="$PROJECT_ID:northamerica-northeast1:allocat-db"
fi

# Create or update secrets
echo ""
echo "Creating/updating secrets..."

# JWT Secret
if gcloud secrets describe jwt-secret --project=$PROJECT_ID &>/dev/null; then
    echo -n "$JWT_SECRET" | gcloud secrets versions add jwt-secret --data-file=-
    echo "✅ Updated jwt-secret"
else
    echo -n "$JWT_SECRET" | gcloud secrets create jwt-secret --data-file=-
    echo "✅ Created jwt-secret"
fi

# Database Password
if gcloud secrets describe db-password --project=$PROJECT_ID &>/dev/null; then
    echo -n "$DB_PASSWORD" | gcloud secrets versions add db-password --data-file=-
    echo "✅ Updated db-password"
else
    echo -n "$DB_PASSWORD" | gcloud secrets create db-password --data-file=-
    echo "✅ Created db-password"
fi

# Database Username
if gcloud secrets describe db-user --project=$PROJECT_ID &>/dev/null; then
    echo -n "$DB_USER" | gcloud secrets versions add db-user --data-file=-
    echo "✅ Updated db-user"
else
    echo -n "$DB_USER" | gcloud secrets create db-user --data-file=-
    echo "✅ Created db-user"
fi

# Cloud SQL Connection Name
if gcloud secrets describe cloudsql-connection-name --project=$PROJECT_ID &>/dev/null; then
    echo -n "$CONNECTION_NAME" | gcloud secrets versions add cloudsql-connection-name --data-file=-
    echo "✅ Updated cloudsql-connection-name"
else
    echo -n "$CONNECTION_NAME" | gcloud secrets create cloudsql-connection-name --data-file=-
    echo "✅ Created cloudsql-connection-name"
fi

echo ""
echo "✅ All secrets created/updated successfully!"
echo ""
echo "To grant Cloud Run access to secrets, run:"
echo "  gcloud secrets add-iam-policy-binding jwt-secret --member='serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com' --role='roles/secretmanager.secretAccessor'"
echo ""
echo "Or use the Cloud Console to grant access to the Cloud Run service account."
