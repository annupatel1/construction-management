"""
backend_api.py - FastAPI Backend for Multimodal Construction Intelligence Platform
Strict Pydantic JSON contracts, CORS support, and REST endpoints for all Epics.
Platform ID: 6ac4c98d1dcde6d7857c1642
"""

from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from PIL import Image
import io

from ai_engine import analyze_ppe_safety, analyze_concrete_crack, get_chatbot_response
from planning_engine import calculate_project_metrics, STRUCTURE_BENCHMARKS
from weather_engine import get_weather_advisory, WEATHER_PROTOCOLS

app = FastAPI(
    title="Multimodal Construction Intelligence API",
    description="FastAPI REST Services for OSHA PPE Audits, Crack Diagnostics, Real Estate Valuation in INR, and Weather Advisories",
    version="1.0.0"
)

# Enable CORS for React/Vite and external clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------------------------------------------------------
# PYDANTIC SCHEMAS (DATA CONTRACTS)
# ---------------------------------------------------------

class PPEDetectionItem(BaseModel):
    item: str
    status: str
    compliance_rate: Optional[str] = "N/A"
    observations: str

class SiteHazard(BaseModel):
    title: str
    severity: str
    location: Optional[str] = "Site Area"
    standard_violated: Optional[str] = "OSHA 1926"
    corrective_remedy: str

class PPEAuditResponse(BaseModel):
    safety_score: int = Field(ge=0, le=100)
    overall_status: str
    workers_detected: int
    ppe_detection: List[PPEDetectionItem]
    hazards_identified: List[SiteHazard]
    auditor_summary: str
    mode: str

class RepairStep(BaseModel):
    step: int
    method: str
    materials: str
    specification: str

class CrackDiagnosticResponse(BaseModel):
    defect_type: str
    estimated_width_mm: float
    severity: str
    structural_risk: str
    is_code_compliant: bool
    code_limit_reference: str
    root_causes: List[str]
    recommended_repairs: List[RepairStep]
    urgency: str
    engineer_notes: str
    mode: str

class PlanningValuationRequest(BaseModel):
    structure_type: str = "Residential High-Rise (RCC G+20)"
    built_up_sqft: float = 25000.0
    location_tier: str = "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)"
    completion_percentage: float = 45.0

class MaterialBreakdown(BaseModel):
    structural_steel_tonnes: float
    cement_bags_50kg: int
    river_m_sand_cft: int
    coarse_aggregates_cft: int

class MilestoneData(BaseModel):
    current_phase: str
    phase_status: str
    progress_percent: float

class VisualizationData(BaseModel):
    prompt: str
    image_url: str

class PlanningValuationResponse(BaseModel):
    structure_type: str
    built_up_sqft: float
    location_tier: str
    completion_percentage: float
    total_budget_inr: float
    total_budget_formatted: str
    capex_incurred_inr: float
    capex_incurred_formatted: str
    market_rate_per_sqft: float
    current_asset_valuation_inr: float
    current_asset_valuation_formatted: str
    full_market_valuation_inr: float
    full_market_valuation_formatted: str
    materials: MaterialBreakdown
    milestone: MilestoneData
    visualization: VisualizationData

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    prompt: str
    history: Optional[List[ChatMessage]] = []
    api_key: Optional[str] = None

class ChatResponse(BaseModel):
    reply: str
    source: str = "CivicPulse AI Engine"


# ---------------------------------------------------------
# REST ENDPOINTS
# ---------------------------------------------------------

@app.get("/api/health")
def health_check():
    """Health check endpoint."""
    return {
        "status": "healthy",
        "service": "Multimodal Construction Intelligence Platform",
        "platform_id": "6ac4c98d1dcde6d7857c1642",
        "version": "1.0.0"
    }


@app.post("/api/ppe-audit", response_model=PPEAuditResponse)
async def ppe_audit_endpoint(
    file: UploadFile = File(...),
    api_key: Optional[str] = Form(None)
):
    """
    Multimodal PPE Safety Audit Endpoint.
    Analyzes site photo under OSHA 1926 and IS 456 standards.
    """
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Invalid file type. Please upload a JPG or PNG image.")
    
    contents = await file.read()
    try:
        image = Image.open(io.BytesIO(contents))
    except Exception:
        raise HTTPException(status_code=400, detail="Corrupted or unreadable image file.")
        
    result = analyze_ppe_safety(image, api_key=api_key)
    return result


@app.post("/api/crack-diagnostics", response_model=CrackDiagnosticResponse)
async def crack_diagnostics_endpoint(
    file: UploadFile = File(...),
    api_key: Optional[str] = Form(None)
):
    """
    Forensic Concrete Crack Diagnostics Endpoint.
    Classifies crack typology, measures aperture, and returns repair protocols under IS 456 / ACI 224R.
    """
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Invalid file type. Please upload a JPG or PNG image.")
    
    contents = await file.read()
    try:
        image = Image.open(io.BytesIO(contents))
    except Exception:
        raise HTTPException(status_code=400, detail="Corrupted or unreadable image file.")
        
    result = analyze_concrete_crack(image, api_key=api_key)
    return result


@app.post("/api/planning-valuation", response_model=PlanningValuationResponse)
def planning_valuation_endpoint(req: PlanningValuationRequest):
    """
    Project Estimation and Real Estate Valuation in INR (Lakhs and Crores).
    """
    metrics = calculate_project_metrics(
        structure_type=req.structure_type,
        built_up_sqft=req.built_up_sqft,
        location_tier=req.location_tier,
        completion_percentage=req.completion_percentage
    )
    return metrics


@app.get("/api/weather-advisory")
def weather_advisory_endpoint(
    condition: str = Query("Normal Fair-Weather Conditions (22°C - 30°C)")
):
    """
    Weather-Aware Safety Alerts and Concrete Hydration Protocols.
    """
    return get_weather_advisory(condition)


@app.get("/api/weather-conditions")
def weather_conditions_list():
    """List available weather condition presets."""
    return list(WEATHER_PROTOCOLS.keys())


@app.post("/api/chat", response_model=ChatResponse)
def chat_endpoint(req: ChatRequest):
    """
    Multi-turn Construction Engineering Assistant Chatbot.
    """
    raw_history = [{"role": m.role, "content": m.content} for m in req.history]
    reply = get_chatbot_response(req.prompt, raw_history, api_key=req.api_key)
    return ChatResponse(reply=reply)
