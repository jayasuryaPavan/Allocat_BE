#!/bin/bash

# GCP Deployment Script for Allocat ERP
# Usage: ./gcp-deploy.sh [project-id]

set -e

PROJECT_ID=${1:-${GOOGLE_CLOUD_PROJECT}}
REGION="northamerica-northeast1"
SERVICE_NAME="allocat-erp"
IMAGE_NAME="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"

if [ -z "$PROJECT_ID" ]; then
    echo "Error: Project ID is required"
    echo "Usage: ./gcp-deploy.sh [project-id]"
    echo "Or set GOOGLE_CLOUD_PROJECT environment variable"
    exit 1
fi

echo "🚀 Starting deployment to GCP..."
echo "Project ID: $PROJECT_ID"
echo "Region: $REGION"
echo "Service: $SERVICE_NAME"

# Set the project
gcloud config set project $PROJECT_ID

# Enable required APIs
echo "📦 Enabling required GCP APIs..."
gcloud services enable \
    cloudbuild.googleapis.com \
    run.googleapis.com \
    sqladmin.googleapis.com \
    secretmanager.googleapis.com \
    containerregistry.googleapis.com

# Build and push Docker image
echo "🔨 Building Docker image..."
gcloud builds submit \
    --tag $IMAGE_NAME:latest \
    --tag $IMAGE_NAME:$(git rev-parse --short HEAD 2>/dev/null || echo "latest") \
    --project $PROJECT_ID

# Get Cloud SQL connection name
CLOUDSQL_INSTANCE="${PROJECT_ID}:${REGION}:allocat-db"

# Check if Cloud SQL instance exists
if ! gcloud sql instances describe allocat-db --project=$PROJECT_ID &>/dev/null; then
    echo "⚠️  Warning: Cloud SQL instance 'allocat-db' not found!"
    echo "Please run ./cloudsql-setup.sh first"
    exit 1
fi

# Deploy to Cloud Run
echo "🚢 Deploying to Cloud Run..."
gcloud run deploy $SERVICE_NAME \
    --image $IMAGE_NAME:latest \
    --region $REGION \
    --platform managed \
    --allow-unauthenticated \
    --port 8081 \
    --memory 2Gi \
    --cpu 2 \
    --min-instances 1 \
    --max-instances 10 \
    --set-env-vars SPRING_PROFILES_ACTIVE=prod \
    --add-cloudsql-instances $CLOUDSQL_INSTANCE \
    --set-secrets SPRING_DATASOURCE_PASSWORD=db-password:latest,SPRING_DATASOURCE_USERNAME=db-user:latest,JWT_SECRET=jwt-secret:latest,CLOUDSQL_INSTANCE=cloudsql-connection-name:latest \
    --project $PROJECT_ID

# Get the service URL
SERVICE_URL=$(gcloud run services describe $SERVICE_NAME \
    --region $REGION \
    --format 'value(status.url)' \
    --project $PROJECT_ID)

echo "✅ Deployment complete!"
echo "Service URL: $SERVICE_URL"
echo ""
echo "Next steps:"
echo "1. Test the health endpoint: $SERVICE_URL/actuator/health"
echo "2. Update your DNS to point to this URL (or set up a custom domain)"
echo "3. Update frontend environment with this backend URL"
echo "4. Verify database migrations ran successfully"
echo ""
echo "To check logs:"
echo "  gcloud run services logs read $SERVICE_NAME --region $REGION"
