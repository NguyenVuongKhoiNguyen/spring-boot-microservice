@echo off
echo =========================================
echo Starting Microservices Infrastructure...
echo =========================================

echo.
echo Starting User Database and running Flyway migrations...
cd user
docker-compose --env-file ../.env -f docker-compose.db.yaml up -d
cd ..

echo.
echo Starting Hotel Database and running Flyway migrations...
cd hotel
docker-compose --env-file ../.env -f docker-compose.db.yaml up -d
cd ..

echo.
echo Starting Payment Database and running Flyway migrations...
cd payment
docker-compose --env-file ../.env -f docker-compose.db.yaml up -d
cd ..

echo.
echo Starting Notification Database and running Flyway migrations...
cd notification
docker-compose --env-file ../.env -f docker-compose.db.yaml up -d
cd ..

echo.
echo Starting MinIO Object Storage and initializing buckets...
cd minio
docker-compose --env-file ../.env -f docker-compose.minio.yaml up -d
cd ..

echo.
echo =========================================
echo Infrastructure started successfully!
echo You can check Docker Desktop to see the containers.
echo =========================================
pause
