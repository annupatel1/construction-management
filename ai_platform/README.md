# 🏗️ Multimodal Construction Intelligence Platform
> **Skill Wallet Capstone Project 1**  
> **Platform ID:** `6ac4c98d1dcde6d7857c1642`  
> **Category:** Generative AI | Real Estate Decision Support | Construction Safety Engineering

---

## 📌 Executive Summary

The **Multimodal Construction Intelligence Platform** is an enterprise-grade AI decision-support system built for site engineers, project managers, safety auditors, and real-estate developers. It integrates multimodal computer vision (Google Gemini 1.5 Pro / Flash) with civil engineering domain rules compliant with **OSHA 1926 CFR**, **IS 456:2000**, and the **National Building Code (NBC) of India 2016**.

Zero external database dependencies ensure seamless **1-click deployment on Streamlit Community Cloud, Render, or Docker**.

---

## 🌟 Key Features & Epic Coverage

### 🦺 1. Site Safety & PPE Vision Audit (Epic 2)
- **Computer Vision Model:** Google Gemini 1.5 Multimodal Vision with high-fidelity demonstration mode fallback.
- **Safety Compliance Check:** Real-time auditing for **Hard Hats (IS 2925 / OSHA 1926.100)**, **High-Vis Vests (IS 15809)**, **Steel-Toe Boots (IS 15298)**, and **Fall Arrest Harnesses (OSHA 1926.502)**.
- **Structured JSON Output:** Returns `safety_score` (0-100), `overall_status`, itemized PPE checklist, and identified physical hazards with severity classifications (**Critical**, **High**, **Medium**, **Low**) and actionable engineering remedies.
- **Downloadable Reports:** Direct export to structured JSON.

### 🔬 2. Crack Diagnostics & Structural Integrity (Epic 2)
- **Forensic Fracture Analysis:** Classifies defect typologies: **Hairline**, **Settlement**, **Structural Shear**, **Thermal Expansion**, and **Spalling / Delamination**.
- **IS 456:2000 Clause 35.3.2 Verification:** Evaluates crack width against statutory exposure thresholds (0.3mm mild, 0.2mm moderate, 0.1mm severe).
- **Engineering Repair Protocols:** Generates multi-step repair workflows specifying low-viscosity epoxy injection (Sikadur 52 / Nitofil), CFRP carbon fiber wrapping (ACI 440.2R), or structural microfine cement re-grouting.

### 📊 3. Planning & Real Estate Valuation in INR (Epic 3)
- **Indian Real Estate Economics:** Dynamic calculations for Tier 1 (Mumbai, Delhi-NCR, Bengaluru), Tier 2 (Pune, Hyderabad, Chennai), and Tier 3 cities.
- **Key Financial Outputs:**
  - Total Construction Budget in INR (formatted in **Lakhs** and **Crores**).
  - CapEx Incurred to Date based on milestone progress.
  - Current WIP Asset Valuation and Full Completed Market Valuation.
- **Bill of Materials (BOM) Estimation:** Automated consumption modeling for **Structural Steel (Metric Tonnes)**, **Cement (50kg bags)**, **M-Sand (cu. ft.)**, and **Coarse Aggregates (cu. ft.)**.
- **Generative 3D Architecture:** Dynamic integration with **Pollinations AI / Flux Diffusion** prompt generator to produce photorealistic 8K exterior concepts.

### 🌦️ 4. Daily Site Assistant & Weather-Aware Advisory (Epic 5)
- **Dynamic Weather Presets:** High Wind (>35 km/h), Heavy Rain & Monsoonal Storms, Extreme Heat (>40°C), and Normal Fair Weather.
- **Mandatory Safety Protocols:** Automated stop-work triggers for tower cranes (OSHA 1926.1431), trench flood evacuations (OSHA 1926.651), and electrical ground-fault isolation.
- **Concrete Hydration Directives:** IS 7861 & IS 456 compliant hot/cold-weather curing and placement schedules.
- **Multi-Turn Engineering Chatbot:** Principal Civil Engineer assistant advising on formwork stripping periods, rebar lap splices ($L_d$), and mix designs.

---

## 🏛️ System Architecture

```
[ Site Inspection Images / Project Parameters ]
                      │
                      ▼
 ┌─────────────────────────────────────────────────────────┐
 │       CivicPulse AI Multi-Tier Processing Engine        │
 ├────────────────────────────┬────────────────────────────┤
 │  Multimodal AI Vision      │  Deterministic Engineering │
 │  - Gemini 1.5 Flash / Pro  │  - IS 456:2000 Code Limits │
 │  - OSHA 1926 Rulesets      │  - BOM Quantity Formulas   │
 │  - High-Fidelity Demo Mode │  - INR Currency Models     │
 └────────────────────────────┴────────────────────────────┘
                      │
       ┌──────────────┴──────────────┐
       ▼                             ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│  Turnkey Streamlit App   │  │  FastAPI Backend (8000)  │
│  - Responsive Dark Theme │  │  - Pydantic JSON Schemas │
│  - Interactive Metric UI │  │  - REST Endpoints        │
│  - 1-Click Cloud Deploy  │  │  - React / Vite Client   │
└──────────────────────────┘  └──────────────────────────┘
```

---

## 🚀 Quick Start & Deployment Guide

### Prerequisites
- Python 3.9+ installed
- Node.js 18+ (optional, for React frontend)

### 1. Fast Run via Shell Script
```bash
# Make script executable (if not already)
chmod +x start_platform.sh

# Run turnkey platform
./start_platform.sh 1
```
*Options:*
- `1`: Turnkey Streamlit App (Port 8501)
- `2`: FastAPI REST Backend (Port 8000)
- `3`: Full Stack (FastAPI + React Frontend)
- `4`: Streamlit App + FastAPI Backend concurrently

---

### 2. Manual Startup

#### Step A: Activate Virtual Environment
```bash
source venv/bin/activate
pip install -r requirements.txt
```

#### Step B: Launch Streamlit App
```bash
streamlit run app.py --server.port 8501
```
Visit **`http://localhost:8501`** in your browser.

#### Step C: Launch FastAPI Backend
```bash
uvicorn backend_api:app --host 0.0.0.0 --port 8000 --reload
```
Interactive Swagger API docs available at **`http://localhost:8000/docs`**.

#### Step D: Launch React + Vite Frontend (Optional)
```bash
cd buildtrack/frontend
npm run dev
```

---

### 3. Docker Deployment
```bash
# Build Docker image
docker build -t construction-intelligence-platform .

# Run container
docker run -p 8501:8501 -p 8000:8000 construction-intelligence-platform
```

---

## 🧪 Automated Testing

Run the comprehensive unit, schema contract, and edge-case test suite:
```bash
venv/bin/python3 test_suite.py
```
**Test Coverage:**
- `test_health_check`: Validates platform ID and system status.
- `test_ppe_safety_audit`: Validates score bounds (0-100), PPE checklist items, and hazard schemas.
- `test_crack_diagnostics`: Validates forensic defect typologies and repair step models.
- `test_planning_and_valuation_calculations`: Validates CapEx, BOM quantity formulas, and Pollinations URLs.
- `test_format_inr`: Validates INR formatting in Lakhs and Crores.
- `test_weather_advisory_protocols`: Validates safety alerts for all weather conditions.
- `test_chatbot_engineering_responses`: Validates domain-specific civil engineering outputs.
- `test_fastapi_endpoints_integration`: Validates all FastAPI multipart and JSON endpoints.
- `test_edge_cases`: Tests corrupted file uploads and invalid MIME type handling.

---

## 📄 API JSON Data Contracts

### POST `/api/ppe-audit`
```json
{
  "safety_score": 76,
  "overall_status": "Action Required",
  "workers_detected": 4,
  "ppe_detection": [
    {
      "item": "Industrial Hard Hats / Helmets (IS 2925 / OSHA 1926.100)",
      "status": "Detected",
      "compliance_rate": "100%",
      "observations": "All 4 workers wearing certified helmets properly buckled."
    }
  ],
  "hazards_identified": [
    {
      "title": "Unprotected Slab Edge Fall Hazard",
      "severity": "Critical",
      "location": "North-West perimeter, Level 3 cantilever slab",
      "standard_violated": "OSHA 1926.501(b)(1) & NBC 2016 Part 7",
      "corrective_remedy": "Immediately install 42-inch guardrails with toe-boards."
    }
  ],
  "auditor_summary": "Perimeter fall protection is deficient and requires halting edge works.",
  "mode": "GEMINI_1.5_LIVE"
}
```

---

## 🛡️ License & Platform Information
- **Platform ID:** `6ac4c98d1dcde6d7857c1642`
- **Standards:** OSHA 1926, IS 456:2000, NBC 2016, IRC, ACI 224R
- **Built with:** Python 3.14 / 3.9+, Streamlit, FastAPI, Pydantic, Pillow, Pollinations AI, React.js, Vite
