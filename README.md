# 📊 SaaS Foundry API

A Spring Boot-based multi-tenant SaaS platform backed by PostgreSQL and secured via Keycloak.  
Provides database-level isolation per organization and realm-level isolation per client for robust security and scalability.

---

## 🚀 Overview

This API powers the backend of a SaaS Foundry platform. It enables:

- Super Admins to create **Organizations** (e.g., Airtel)
- Organization Admins to create **Clients** (e.g., Airtel India, Airtel Africa)

### 🔁 End-to-End Flow

1. A **Super Admin** creates an **Organization** (e.g., Airtel).
2. For every Organization:
   - A **dedicated PostgreSQL database** is provisioned.
3. An **Org Admin** (associated with the Organization) creates **Clients** (e.g., Airtel India).
4. For every Client:
   - A **Keycloak realm** is created.
   - Users of that client are created inside the client’s realm.
   - A **Client-specific database** may also be created (if applicable).

This approach ensures **strong data isolation**, **multi-tenancy**, and **security** across organizations and clients.

---

## 🧰 Tech Stack

| Tech             | Purpose                                 |
|------------------|-----------------------------------------|
| Spring Boot      | RESTful API Backend                     |
| PostgreSQL       | Multi-tenant Data Storage               |
| Keycloak         | Authentication & Realm Management       |
| Docker Compose   | Local development and service orchestration |

---

## ⚙️ Setup Instructions

#### 🔧 Prerequisites
Ensure you have Docker and Docker Compose installed.

#### Setup PostGres, RabbitMQ and Keycloak
##### 📁 Environment Configuration
1. Copy the environment file template:
   ```bash
   cp ./deployment/example.env ./deployment/.prod.env
   ```

2. Edit `.prod.env` to include environment-specific credentials, such as DB URL, Keycloak secrets, etc.


#### 🚀 Start Docker Containers
```bash
chmod +x setup.sh
./setup.sh
```

#### Setup Saas Foundry API
##### 📁 Environment Configuration
1. Copy the environment file template:
   ```bash
   cp ./example.env ./.prod.env
   ```

2. Edit `.prod.env` to include environment-specific credentials, such as DB URL, Keycloak secrets, etc.

##### 🔑 Configure Keycloak Client Secret

1. Open [Keycloak Admin Console](http://localhost:8080).
2. Navigate to:
   ```
   Realm: master → Clients → saas-admin → Credentials
   ```
3. Copy the **Client Secret**.
4. Paste it into `KEYCLOAK_CLIENT_SECRET` in `.prod.env`.

#### 🚀 Start Docker Container
Load environment variables:

```bash
export $(cat .local.env | xargs)    # For local development
# OR
export $(cat .prod.env | xargs)     # For production
```

Start all services:

```bash
docker-compose up -d --build
```

---

### 🔐 Generate Access Token
#### SaaS Admin
To create Organizations, the SaaS Admin can login with the below request. Replace `<USERNAME>` and `<PASSWORD>` with the values of `KEYCLOAK_SAAS_ADMIN_USERNAME` and `KEYCLOAK_SAAS_ADMIN_PASSWORD` set in your `.env`:

```bash
curl --location --request POST 'http://localhost:8080/realms/master/protocol/openid-connect/token' \
   --header 'Content-Type: application/x-www-form-urlencoded' \
   --data-urlencode 'grant_type=password' \
   --data-urlencode 'client_id=saas-admin' \
   --data-urlencode 'username=<USERNAME>' \
   --data-urlencode 'password=<PASSWORD>'
```

#### Organization Admin
To create Clients, the Organization Admin can login with the below request.

```bash
curl --location --request POST 'http://localhost:8080/realms/saas-organization/protocol/openid-connect/token' \
   --header 'Content-Type: application/x-www-form-urlencoded' \
   --data-urlencode 'grant_type=password' \
   --data-urlencode 'client_id=organization-admin' \
   --data-urlencode 'username=<USERNAME>' \
   --data-urlencode 'password=<PASSWORD>'
```

Note: These requests are only for authentication. Not authorizatioň
---

### 📡 Example API Request

```bash
curl -X 'GET'   'http://localhost:6060/api/organizations?page=0&size=20' \
   -H 'Authorization: Bearer <ACCESS_TOKEN>' \
   -H 'accept: application/json'
```

---

## 📚 API Documentation

- **Swagger UI**: [http://localhost:6060/api/swagger-ui/index.html](http://localhost:6060/api/swagger-ui/index.html)  
- **Health Check**: [http://localhost:6061/actuator/health](http://localhost:6061/actuator/health)

---