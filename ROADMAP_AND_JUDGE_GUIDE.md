# Suraksha Saathi — Backend Architecture & Phase 3 Roadmap (Judge Presentation Guide)

---

## 1. Backend Service Overview (Step 10 / Section 9)

The standalone threat intelligence service is located in the [`backend/`](file:///c:/Users/mudul/OneDrive/Documents/Desktop/TATA/backend) folder.

### Core Philosophy: Privacy-Preserving Threat Intelligence
Unlike traditional surveillance solutions, Suraksha Saathi **never sends private SMS, screen transcripts, or family communications to the cloud**. 
All threat heuristics, regex engines, and risk calculations run **on-device**. The backend is solely queried for:
1. Anonymized phone number reputation lookups
2. Phishing & malicious domain reputation lookups
3. Community fraud reports
4. Delta threat intelligence synchronization

### The 5 Verified Endpoints

| Endpoint | Method | Purpose | Input / Output |
| :--- | :---: | :--- | :--- |
| `/check-number` | `POST` | Query reported scam score & category | `{ "number": "+919876543210" }` &rarr; `riskScore`, `confidence`, `category` |
| `/check-domain` | `POST` | Check phishing / malicious link reputation | `{ "domain": "sbi-login-verification.xyz" }` &rarr; `riskScore`, `confidence` |
| `/report-number` | `POST` | Register community scam number report | Recalibrates Bayesian confidence & risk score dynamically |
| `/report-domain` | `POST` | Register community malicious link report | Recalibrates risk score dynamically |
| `/threat-updates` | `GET` | Delta sync for offline Android client | `?since_timestamp=...` &rarr; Returns latest threat feed |

### Backend Technology Stack
- **Framework**: FastAPI (async Python 3.11)
- **Database**: SQLAlchemy ORM with dual-engine support:
  - **SQLite** (`suraksha_saathi.db`): Zero-configuration out of the box for instant judging demos.
  - **PostgreSQL**: Production-grade via `DATABASE_URL` environment variable.
- **Scoring Engine**: Bayesian saturation curve with category-weighted penalties (Digital Arrest &rarr; +25 boost, Bank KYC &rarr; +20 boost).
- **Deployment**: `Dockerfile` and `docker-compose.yml` for 1-click container orchestration.

---

## 2. Android $\leftrightarrow$ Backend Hybrid Integration

### Offline-First Architecture
The mobile application operates on a **hybrid offline-first model**:
1. **Zero-Latency Offline Shield**: Call screening and link verification first consult [`LocalReputationDb.kt`](file:///c:/Users/mudul/OneDrive/Documents/Desktop/TATA/app/src/main/java/com/example/surakshasaathi/riskengine/LocalReputationDb.kt) instantaneously with no network delay.
2. **Background Delta Sync**: When connected, [`SurakshaApiClient.kt`](file:///c:/Users/mudul/OneDrive/Documents/Desktop/TATA/app/src/main/java/com/example/surakshasaathi/network/SurakshaApiClient.kt) fetches delta threat updates and merges new scam intelligence directly into the local device cache.
3. **Instant Community Reporting**: Submitting a report updates the device shield immediately (0 ms UI wait) and asynchronously queues transmission to the cloud backend.
4. **Live UI Status**: The [`CommunityReportScreen.kt`](file:///c:/Users/mudul/OneDrive/Documents/Desktop/TATA/app/src/main/java/com/example/surakshasaathi/ui/community/CommunityReportScreen.kt) features a live **Cloud Threat Engine** card displaying sync status and allowing judges to test delta synchronization with a single tap.

---

## 3. Phase 3 Roadmap & Future Scope (Pitch to Judges)

During the hackathon presentation, pitch these 3 strategic pillars as our production enterprise expansion roadmap:

### Pillar 1: Bank & Telco Direct Carrier Integration (STIR/SHAKEN + Signaling)
- **Challenge Today**: Scammers use VoIP spoofing and unallocated SIM farms to display legitimate bank customer care numbers.
- **Future Solution**: 
  - Partner with telecom operators (Airtel, Jio, Vi) via **STIR/SHAKEN cryptographic call verification** to cryptographically validate call origination at the SIP signaling layer.
  - Integrate with core banking fraud APIs (RBI MuleHunter initiative) so when an account is flagged as an active mule account, inbound calls associated with that account are auto-silenced across the subscriber base.

### Pillar 2: On-Device Voice-Clone & Deepfake Audio Detection
- **Challenge Today**: "Digital arrest" and "kidnapping" scams increasingly leverage 3-second generative voice clones of family members (e.g., ElevenLabs / XTTS).
- **Future Solution**:
  - Deploy a lightweight, quantized spectrogram CNN model (TensorFlow Lite / ONNX Mobile, < 8 MB) running locally on Android Neural Networks API (NNAPI).
  - Analyze micro-artifacts in audio: unnatural vocal tract acoustics, synthetic frequency cutoffs, and missing respiratory inhalation pauses.
  - Trigger an immediate **"Potential Synthetic / AI Voice Detected"** voice alert in the senior's regional language.

### Pillar 3: Direct NCRP (National Cyber Crime Reporting Portal) Integration
- **Challenge Today**: Victims often wait 24–48 hours before reporting to cyber crime cells, by which time funds have cycled through multiple mule accounts.
- **Future Solution**:
  - Integrate with the **MHA Indian Cyber Crime Coordination Centre (I4C)** and **National Cyber Crime Helpline (1930)** API.
  - With senior's one-touch consent, dispatch structured fraud incident telemetry (scammer's VPA, bank IFSC, phone number, call timestamp, SMS evidence) directly into NCRP's Automated Golden Hour interbank freeze pipeline.
