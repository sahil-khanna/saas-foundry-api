# 📊 CXO Insights API – WhatsApp Query Assistant

An intelligent API service that enables CXOs and managers to ask natural language questions via **WhatsApp** and receive real-time business insights — powered by **[novaai](https://ollama.com/rodolfo/novaai)**, **PostgreSQL**, and the **[Vonage Communication API](https://www.vonage.com/communications-apis/messages/features/whatsapp/)**.

---

## 🚀 Overview

This system allows CXOs to send questions like:

> _"How many calls were answered today?"_

And instantly receive insights like:

> _"A total of 22 inbound calls were answered today."_

### 🔁 End-to-End Flow

1. ✅ CXO sends a WhatsApp message.  
2. ✅ API receives the message via Vonage.  
3. 🤖 Message is forwarded to **novaai** (running locally via **Ollama**) for SQL generation.  
4. 🧠 Generated SQL is executed against **PostgreSQL**.  
5. 📲 The response is sent back to the CXO via WhatsApp.

---

## 🧰 Tech Stack

- **Spring Boot** – RESTful API backend  
- **Ollama + novaai** – Local LLM for SQL generation  
- **PostgreSQL** – Business metrics database  
- **Vonage Communication API** – WhatsApp messaging interface  
- **Docker Compose** – Container orchestration

---

## ⚙️ Setup Instructions

### 📁 Environment Configuration

1. Copy the environment file template:

   ```bash
   cp example.env .prod.env
   ```
Edit .prod.env with your environment-specific credentials and keys.

---

### 🧠 ML Model: novaai via Ollama
1. Install Ollama on your local machine.
2. Pull the fine-tuned novaai model:

   ```bash
   ollama pull rodolfo/novaai
   ```

3. Run the model:

   ```bash
   ollama run rodolfo/novaai
   ```

---

### 🐳 Docker Setup

#### 🔧 Setup prerequisites
```
chmod +x setup.sh
./setup.sh
```

#### 🔧 Copy Keycloak client-secret
- Login to Keycloak admin panel.
- `master` realm > `clients` > `saas-admin` > `Credentials`.
- Copy the `Client Secret` and paste in `KEYCLOAK_CLIENT_SECRET` of the environment files.

#### 🔧 Start the Service

   ```bash
   # Load environment variables
   export $(cat .local.env | xargs)  # For local development
   # OR
   export $(cat .prod.env | xargs)   # For production
   ```

---

#### Build and start the containers
   ```bash
   docker-compose up -d
   ```
---

## 📚 API Documentation
Swagger UI:
http://localhost:6060/api/swagger-ui/index.html

Health Check:
http://localhost:6061/actuator/health

---

## 🧪 Sample Data Generation
To generate and populate sample data for testing:

Clone the data generation script repository:
🔗 [GitHub – Data Generation Script](https://github.com/sahil-khanna-vonage/vonage-hackathon-2025-data-generation-script)

Follow the instructions in the README to insert realistic call metrics into your PostgreSQL database.

---