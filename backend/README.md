# Suraksha Saathi — Threat Intelligence & Reputation Backend

**FastAPI + SQLAlchemy (PostgreSQL / SQLite)** service designed for privacy-preserving scam intelligence synchronization.

---

## 🔒 Privacy-Preserving Architecture
As specified in Section 9 of the system architecture:
- **Zero Raw Message Transmission**: Private SMS, screen content, and family conversations are **NEVER** transmitted to this backend.
- **Scam Reputation Only**: The server is queried only for phone number reputation and domain safety scores.
- **Offline First**: The mobile client caches all known threats into `LocalReputationDb.kt` and queries the server asynchronously to refresh delta threat intelligence or submit user-flagged scam reports.

---

## 📡 The 5 Core API Endpoints

### 1. `POST /check-number`
Checks phone number against community scam databases and calculates risk score.
```json
// Request
{
  "number": "+919876543210"
}

// Response
{
  "number": "+919876543210",
  "category": "Fake bank call — pretends to be SBI customer care",
  "riskScore": 80,
  "reportCount": 45,
  "confidence": 0.93,
  "isScam": true,
  "updatedAt": 1728310000000
}
```

### 2. `POST /check-domain`
Checks URL/domain against known phishing, smishing, and APK malware distribution registries.
```json
// Request
{
  "domain": "https://sbi-login-verification.xyz/kyc"
}

// Response
{
  "domain": "sbi-login-verification.xyz",
  "category": "Fake bank phishing portal stealing credentials",
  "riskScore": 95,
  "reportCount": 112,
  "confidence": 0.97,
  "isScam": true,
  "updatedAt": 1728310000000
}
```

### 3. `POST /report-number`
Community reporting endpoint. Automatically recalibrates risk scores and statistical confidence using Bayesian saturation curves.
```json
// Request
{
  "number": "+919123456789",
  "category": "Digital arrest scam - pretending to be Mumbai police",
  "description": "Video call demand for bail money",
  "reporterId": "anon_device_token"
}
```

### 4. `POST /report-domain`
Submits malicious link reports with domain normalization.
```json
// Request
{
  "domain": "electricity-bill-pay.site",
  "category": "Extortion portal threatening power cut at 9 PM",
  "description": "SMS arrived with urgent warning",
  "reporterId": "anon_device_token"
}
```

### 5. `GET /threat-updates`
Delta synchronization endpoint. Clients supply `since_timestamp` to receive only new or updated threat records.
```http
GET /threat-updates?since_timestamp=1728300000000
```

---

## 🚀 Running the Backend

### Option A: Local Python (Zero Setup / SQLite)
```bash
cd backend
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000
```
Interactive Swagger API documentation will be available at:
`http://localhost:8000/docs`

### Option B: Docker Compose (PostgreSQL)
```bash
cd backend
docker-compose up --build -d
```

### Option C: Run Automated Tests
```bash
cd backend
python test_api.py
```
