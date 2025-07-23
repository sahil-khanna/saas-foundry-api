#!/bin/bash

# Export environment variables
export $(cat .prod.env | xargs)

# Start dependencies
docker network create --driver bridge saas-network
docker-compose -f ./deployment/docker-compose-dependencies.yml up -d

# Wait for Keycloak to become healthy or ready
echo "Waiting for Keycloak to become ready..."

sleep 30

# echo "Keycloak is ready. Proceeding with init script..."

# Run init script
chmod +x ./deployment/keycloak-init/init.sh
docker cp ./deployment/keycloak-init/init.sh keycloak:/tmp/
docker exec -it keycloak bash /tmp/init.sh