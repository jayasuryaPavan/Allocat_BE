#!/bin/bash

echo "========================================"
echo "Allocat ERP - Service Restart Script"
echo "========================================"
echo ""

echo "Stopping any running Java processes on ports 8080 and 8081..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8081 | xargs kill -9 2>/dev/null || true
sleep 2

echo ""
echo "Building projects..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Build failed! Please check the errors above."
    exit 1
fi

echo ""
echo "Starting Backend API on port 8081..."
cd allocat-api
mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
cd ..

echo "Waiting 10 seconds for backend to initialize..."
sleep 10

echo ""
echo "Starting Gateway on port 8080..."
cd allocat-gateway
mvn spring-boot:run > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

echo ""
echo "========================================"
echo "Services are starting!"
echo "========================================"
echo ""
echo "Backend API:  http://localhost:8081  (PID: $BACKEND_PID)"
echo "Gateway:      http://localhost:8080  (PID: $GATEWAY_PID)"
echo ""
echo "Logs:"
echo "  Backend: logs/backend.log"
echo "  Gateway: logs/gateway.log"
echo ""
echo "To stop services:"
echo "  kill $BACKEND_PID $GATEWAY_PID"
echo ""
echo "To view logs in real-time:"
echo "  tail -f logs/backend.log"
echo "  tail -f logs/gateway.log"

