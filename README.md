# jenkins-ai-pipeline-generator
This repo helps to create a serverless AI-powered CI/CD pipeline generator using AWS Lambda, API Gateway, and S3 with near-zero infrastructure cost.

# 🚀 AI Jenkins Pipeline Generator (Serverless on AWS)

## 📌 Overview

This project is an AI-powered Jenkins Pipeline Generator that creates production-ready Jenkinsfiles based on user input.

It is built using:

* FastAPI (backend)
* AWS Lambda (serverless compute)
* API Gateway (API exposure)
* S3 (frontend hosting)
* OpenAI API (AI generation)

---

## 🏗️ Architecture

User → S3 (Frontend) → API Gateway → Lambda → OpenAI → Response

---

## ⚙️ Features

* Generate Jenkins pipelines for Node.js, Java, Python
* Supports Docker and EC2 deployment
* Serverless (low cost)
* Scalable and production-ready

---

## 📦 Backend Setup

### 1. Install dependencies

```bash
pip install -r requirements.txt
```

### 2. Run locally

```bash
uvicorn api:app --reload
```

---

## 🐳 Docker Build

```bash
docker build -t jenkins-ai-generator .
```

---

## ☁️ AWS Deployment (Manual)

### Step 1: Create ECR Repo

```bash
aws ecr create-repository --repository-name jenkins-ai-generator
```

### Step 2: Push Docker Image

```bash
aws ecr get-login-password | docker login --username AWS --password-stdin <ECR_URI>
docker tag jenkins-ai-generator:latest <ECR_URI>:latest
docker push <ECR_URI>:latest
```

### Step 3: Create Lambda

* Use container image from ECR
* Set memory: 1024 MB
* Timeout: 30 sec

### Step 4: API Gateway

* Create HTTP API
* Add POST /generate route
* Integrate with Lambda

### Step 5: S3 Hosting

* Upload frontend
* Enable static hosting

---

## 🌍 Terraform Deployment

### 1. Initialize

```bash
cd terraform
terraform init
```

### 2. Apply

```bash
terraform apply
```

---

## 🔐 Environment Variables

Set in Lambda:

```
OPENAI_API_KEY=your_api_key
```

---

## 💰 Cost Optimization

* Uses serverless architecture
* Free tier eligible
* Estimated cost: $0–$5/month

---

## 📈 Future Improvements

* Add authentication (Cognito)
* Add payment integration
* Kubernetes pipeline support
* GitHub repo auto-detection

---

## 🤝 Contributing

Feel free to fork and enhance the project!

---

## 📧 Contact

For collaboration or freelance work, reach out via LinkedIn.
