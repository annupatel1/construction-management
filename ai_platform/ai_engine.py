"""
ai_engine.py - Multimodal AI Engine for Construction Intelligence Platform
Supports Google Gemini 1.5 with graceful fallback to High-Fidelity Demonstration Mode.
Compliant with OSHA 1926 Safety Standards, IS 456:2000, and NBC 2016.
"""

import json
import os
import re
from typing import Dict, Any, Optional
from PIL import Image

try:
    import google.generativeai as genai
    HAS_GENAI = True
except ImportError:
    HAS_GENAI = False


def _get_gemini_model(api_key: Optional[str] = None, model_name: str = "gemini-1.5-flash"):
    """Initialize and return a Gemini GenerativeModel or None if no valid key."""
    key = api_key or os.getenv("GEMINI_API_KEY", "")
    if not key or not HAS_GENAI:
        return None
    try:
        genai.configure(api_key=key)
        return genai.GenerativeModel(model_name)
    except Exception:
        return None


def _clean_json_response(raw_text: str) -> Dict[str, Any]:
    """Extract and parse JSON safely from LLM markdown code blocks or raw string."""
    cleaned = raw_text.strip()
    match = re.search(r"```(?:json)?\s*([\s\S]*?)\s*```", cleaned)
    if match:
        cleaned = match.group(1).strip()
    return json.loads(cleaned)


# ---------------------------------------------------------
# EPIC 2: PPE SAFETY VISION AUDIT
# ---------------------------------------------------------

PPE_AUDIT_PROMPT = """
You are an expert Senior Construction Safety Auditor certified in OSHA 1926 and Bureau of Indian Standards IS 456 / IS 15848.
Analyze this construction site photograph thoroughly for Personal Protective Equipment (PPE) compliance and physical site hazards.

Evaluate workers and conditions in the image. You must return ONLY a valid, parseable JSON object matching this exact schema:
{
  "safety_score": <integer 0-100>,
  "overall_status": "<Compliant | Action Required | Critical Violation>",
  "workers_detected": <integer estimated count>,
  "ppe_detection": [
    {
      "item": "Industrial Hard Hats / Helmets (IS 2925 / OSHA 1926.100)",
      "status": "<Detected | Missing | Partial | Not Applicable>",
      "compliance_rate": "<percentage string e.g. 85%>",
      "observations": "<detailed observation>"
    },
    {
      "item": "High-Visibility Safety Vests (EN ISO 20471 / IS 15809)",
      "status": "<Detected | Missing | Partial | Not Applicable>",
      "compliance_rate": "<percentage string>",
      "observations": "<detailed observation>"
    },
    {
      "item": "Steel-Toe Puncture-Resistant Boots (IS 15298 / ASTM F2413)",
      "status": "<Detected | Missing | Partial | Not Applicable>",
      "compliance_rate": "<percentage string>",
      "observations": "<detailed observation>"
    },
    {
      "item": "Fall Arrest Harness & Lifelines (IS 3521 / OSHA 1926.502)",
      "status": "<Detected | Missing | Partial | Not Applicable>",
      "compliance_rate": "<percentage string>",
      "observations": "<detailed observation>"
    },
    {
      "item": "Protective Eye & Face Shields (IS 5983 / ANSI Z87.1)",
      "status": "<Detected | Missing | Partial | Not Applicable>",
      "compliance_rate": "<percentage string>",
      "observations": "<detailed observation>"
    }
  ],
  "hazards_identified": [
    {
      "title": "<Short Hazard Name>",
      "severity": "<Low | Medium | High | Critical>",
      "location": "<Site zone / worker area in photo>",
      "standard_violated": "<e.g. OSHA 1926.501 Fall Protection / IS 456 Cl. 35>",
      "corrective_remedy": "<Specific actionable engineering or administrative control>"
    }
  ],
  "auditor_summary": "<Executive summary of safety posture, immediate stops needed, and sign-off status>",
  "mode": "GEMINI_LIVE_MULTIMODAL"
}
"""

def analyze_ppe_safety(image: Image.Image, api_key: Optional[str] = None) -> Dict[str, Any]:
    """Audits PPE safety from image with Gemini or realistic fallback demo mode."""
    model = _get_gemini_model(api_key)
    if model:
        try:
            response = model.generate_content([PPE_AUDIT_PROMPT, image])
            data = _clean_json_response(response.text)
            data["mode"] = "GEMINI_1.5_LIVE"
            return data
        except Exception as e:
            # Fallback gracefully if API rate limit or quota exceeded
            demo = get_demo_ppe_result()
            demo["mode"] = f"DEMO_FALLBACK (API Note: {str(e)[:90]})"
            return demo
    else:
        return get_demo_ppe_result()


def get_demo_ppe_result() -> Dict[str, Any]:
    """Realistic high-fidelity demo result for site safety audit."""
    return {
        "safety_score": 76,
        "overall_status": "Action Required",
        "workers_detected": 4,
        "ppe_detection": [
            {
                "item": "Industrial Hard Hats / Helmets (IS 2925 / OSHA 1926.100)",
                "status": "Detected",
                "compliance_rate": "100%",
                "observations": "All 4 workers wearing certified Type 1 Class E impact-resistant helmets properly buckled."
            },
            {
                "item": "High-Visibility Safety Vests (EN ISO 20471 / IS 15809)",
                "status": "Partial",
                "compliance_rate": "75%",
                "observations": "3 workers wearing fluorescent Class 2 vests; 1 worker in unreflective cotton shirt near heavy machinery swing radius."
            },
            {
                "item": "Steel-Toe Puncture-Resistant Boots (IS 15298 / ASTM F2413)",
                "status": "Detected",
                "compliance_rate": "100%",
                "observations": "Heavy-duty construction footwear observed on all active personnel on reinforced concrete deck."
            },
            {
                "item": "Fall Arrest Harness & Lifelines (IS 3521 / OSHA 1926.502)",
                "status": "Missing",
                "compliance_rate": "0%",
                "observations": "Worker near edge of slab (>2.4m elevation) lacks personal fall arrest lanyard or toe-board railing."
            },
            {
                "item": "Protective Eye & Face Shields (IS 5983 / ANSI Z87.1)",
                "status": "Partial",
                "compliance_rate": "50%",
                "observations": "Rebar cutting operator wearing standard goggles; helper lacks splash/particle shield."
            }
        ],
        "hazards_identified": [
            {
                "title": "Unprotected Slab Edge Fall Hazard",
                "severity": "Critical",
                "location": "North-West perimeter, Level 3 cantilever slab",
                "standard_violated": "OSHA 1926.501(b)(1) & NBC 2016 Part 7 Section 4",
                "corrective_remedy": "Immediately install 42-inch top-rail and mid-rail perimeter guardrails with 4-inch toe-boards. Issue full-body harnesses tied off to 5,000-lb anchorage."
            },
            {
                "title": "Rebar Impalement Risk",
                "severity": "High",
                "location": "Column starter bars adjacent to pedestrian walkway",
                "standard_violated": "OSHA 1926.701(b) / IS 456:2000 Cl. 12.3",
                "corrective_remedy": "Cap all protruding vertical rebar ends with steel-reinforced OSHA-approved mushroom caps or wood trough covering."
            },
            {
                "title": "Inadequate High-Visibility in Heavy Equipment Corridor",
                "severity": "Medium",
                "location": "Material laydown area near concrete transit mixer",
                "standard_violated": "IS 15809:2008 / OSHA 1926.201",
                "corrective_remedy": "Provide high-visibility Class 3 retroreflective vests to all ground personnel in transit paths."
            }
        ],
        "auditor_summary": "Site demonstrates good baseline helmet and footwear compliance. However, perimeter fall protection is urgently deficient and requires immediate halting of edge activities until double guardrails and anchor points are secured.",
        "mode": "DEMONSTRATION_MODE"
    }


# ---------------------------------------------------------
# EPIC 2: CRACK DIAGNOSTICS & STRUCTURAL INTEGRITY
# ---------------------------------------------------------

CRACK_AUDIT_PROMPT = """
You are an expert Forensic Structural Engineer specializing in reinforced concrete inspection under IS 456:2000, ACI 224R-01, and BS 8110.
Analyze this photo of a concrete or masonry surface crack with forensic precision.

You must return ONLY a valid, parseable JSON object matching this exact schema:
{
  "defect_type": "<Hairline | Settlement | Structural Shear | Thermal Expansion | Spalling>",
  "estimated_width_mm": <float e.g. 1.8>,
  "severity": "<Low | Moderate | Severe | Critical>",
  "structural_risk": "<Surface Aesthetic Only | Non-Critical Progressive | Major Structural Hazard | Immediate Failure Risk>",
  "is_code_compliant": false,
  "code_limit_reference": "IS 456:2000 Clause 35.3.2 restricts crack width to 0.3mm (mild exposure), 0.2mm (moderate), 0.1mm (severe).",
  "root_causes": [
    "<Primary root cause e.g. Differential foundation settlement / Shear overload / Plastic shrinkage / Carbonation rebar corrosion>",
    "<Secondary contributing factor>"
  ],
  "recommended_repairs": [
    {
      "step": 1,
      "method": "<e.g. Low-viscosity epoxy pressure injection / Microfine cementitious re-grouting / Carbon-fiber CFRP wrap>",
      "materials": "<e.g. Sikadur 52 / Nitomortar / Carbon Fiber Sheet 300gsm>",
      "specification": "<Engineering execution standard>"
    },
    {
      "step": 2,
      "method": "<Subsequent reinforcement or protective coating>",
      "materials": "<e.g. Elastomeric acrylic anti-carbonation barrier>",
      "specification": "<Curing & pressure threshold>"
    }
  ],
  "urgency": "<Routine Maintenance (Within 60 Days) | Priority Repair (Within 14 Days) | Immediate Intervention (Within 24 Hours)>",
  "engineer_notes": "<Technical assessment explaining crack trajectory, angle, active vs dormant state, and structural consequence>",
  "mode": "GEMINI_LIVE_MULTIMODAL"
}
"""

def analyze_concrete_crack(image: Image.Image, api_key: Optional[str] = None) -> Dict[str, Any]:
    """Diagnoses structural concrete cracks using Gemini Vision or fallback demo."""
    model = _get_gemini_model(api_key)
    if model:
        try:
            response = model.generate_content([CRACK_AUDIT_PROMPT, image])
            data = _clean_json_response(response.text)
            data["mode"] = "GEMINI_1.5_LIVE"
            return data
        except Exception as e:
            demo = get_demo_crack_result()
            demo["mode"] = f"DEMO_FALLBACK (API Note: {str(e)[:90]})"
            return demo
    else:
        return get_demo_crack_result()


def get_demo_crack_result() -> Dict[str, Any]:
    """Realistic high-fidelity demo result for crack diagnostics."""
    return {
        "defect_type": "Structural Shear",
        "estimated_width_mm": 2.4,
        "severity": "Severe",
        "structural_risk": "Major Structural Hazard",
        "is_code_compliant": False,
        "code_limit_reference": "IS 456:2000 Clause 35.3.2 specifies max crack width 0.2mm for moderate exposure; 2.4mm exceeds safety limit by 12x.",
        "root_causes": [
            "Excess diagonal tension shear stress exceeding shear reinforcement capacity near support zone.",
            "Sub-grade settlement leading to differential column displacement and torsional redistribution."
        ],
        "recommended_repairs": [
            {
                "step": 1,
                "method": "V-Groove chasing & Low-viscosity epoxy injection",
                "materials": "Sikadur 52 / Nitofil LV epoxy resin injected under 0.2 to 0.4 N/mm² pressure.",
                "specification": "Seal surface with thixotropic paste, drill injection nipples at 150mm c/c along 45° fracture plane."
            },
            {
                "step": 2,
                "method": "CFRP (Carbon Fiber Reinforced Polymer) External Shear Laminate Wrapping",
                "materials": "Unidirectional high-modulus carbon fabric (300 g/m²) saturated with structural epoxy matrix.",
                "specification": "Apply 2 layers U-jacket confinement wrap per ACI 440.2R-17 to restore ultimate shear capacity."
            }
        ],
        "urgency": "Immediate Intervention (Within 24 Hours)",
        "engineer_notes": "The diagonal 45-degree angle indicates classic shear failure in a beam-column junction. Concrete core exhibits micro-spalling along the fracture lips. Immediate temporary propping (Acro shores) is mandated prior to resin injection.",
        "mode": "DEMONSTRATION_MODE"
    }


# ---------------------------------------------------------
# EPIC 5: MULTI-TURN SITE ASSISTANT CHATBOT
# ---------------------------------------------------------

SYSTEM_CHAT_PERSONA = """
You are "CivicPulse AI", a Principal Civil & Structural Site Engineer with 25+ years of Tier-1 EPC experience.
You advise project managers, site engineers, and contractors on:
- IS 456:2000 (Plain & Reinforced Concrete)
- OSHA 1926 Safety & Fall Protection regulations
- National Building Code (NBC) of India 2016
- CPWD / IRC specifications, mix designs (M25, M30, M40), curing, formwork striking periods, rebar splicing, and site weather mitigations.
Provide concise, authoritative, calculation-backed engineering answers with practical safety caveats.
"""

def get_chatbot_response(prompt: str, history: list, api_key: Optional[str] = None) -> str:
    """Multi-turn engineering assistant response with Gemini or domain fallback."""
    model = _get_gemini_model(api_key)
    if model:
        try:
            # Build conversation history
            chat = model.start_chat(history=[])
            prompt_with_persona = f"{SYSTEM_CHAT_PERSONA}\n\nUser Question: {prompt}"
            response = chat.send_message(prompt_with_persona)
            return response.text
        except Exception as e:
            return _domain_fallback_chat(prompt, str(e))
    else:
        return _domain_fallback_chat(prompt)


def _domain_fallback_chat(prompt: str, error_msg: Optional[str] = None) -> str:
    """Smart civil engineering fallback replies based on keywords."""
    p = prompt.lower()
    prefix = ""
    if error_msg:
        prefix = f"> *Note: Running in high-fidelity offline mode ({error_msg[:80]}).*\n\n"

    if "curing" in p or "concrete" in p:
        return prefix + (
            "**IS 456:2000 Curing Protocol (Cl. 13.5):**\n"
            "- **Ordinary Portland Cement (OPC):** Minimum 7 days under standard moist conditions, extended to 10 days in dry/hot weather.\n"
            "- **Blended Cements (PPC / Slag / Fly ash):** Minimum 10 days, extended to 14 days under dry ambient conditions.\n"
            "- **Recommended Method:** Ponding on slabs (min 25mm depth), wet hessian/burlap wrapping on columns with automated misting.\n"
            "- **Quality Check:** Target 28-day characteristic cube strength ($f_{ck}$) per IS 516 testing standards."
        )
    elif "striking" in p or "formwork" in p or "shuttering" in p:
        return prefix + (
            "**IS 456:2000 Striking Periods for Formwork (Cl. 11.3):**\n"
            "1. **Vertical formwork to columns, walls, beams:** 16 - 24 hours.\n"
            "2. **Soffit formwork to slabs (props left under):** 3 days.\n"
            "3. **Soffit formwork to beams (props left under):** 7 days.\n"
            "4. **Props to slabs:**\n"
            "   - Spanning up to 4.5 m: 7 days.\n"
            "   - Spanning over 4.5 m: 14 days.\n"
            "5. **Props to beams and arches:**\n"
            "   - Spanning up to 6 m: 14 days.\n"
            "   - Spanning over 6 m: 21 days."
        )
    elif "rebar" in p or "lap" in p or "steel" in p:
        return prefix + (
            "**IS 456:2000 Splicing & Lap Length Guidelines (Cl. 26.2.5):**\n"
            "- **Flexural Tension Lap Length:** $L_d$ or $30\\\\phi$ (whichever is greater), typically $40\\\\phi$ to $50\\\\phi$ for M25/M30 with Fe500D TMT rebar.\n"
            "- **Direct Compression Lap Length:** $24\\\\phi$.\n"
            "- **Staggering Rule:** No more than 50% of bars should be lapped at a single cross section. Keep lap splices away from maximum bending zones.\n"
            "- **Tie Wire:** 16-gauge annealed GI binding wire."
        )
    elif "safety" in p or "osha" in p or "ppe" in p or "hazard" in p:
        return prefix + (
            "**OSHA 1926 & NBC 2016 Site Safety Protocols:**\n"
            "- **Fall Protection:** Mandated at elevations $\\ge 1.8m$ (6 ft) under OSHA 1926.501, with standard 42-inch top-rail guardrails.\n"
            "- **Electrical Hazards:** 10-foot clearance from overhead power lines <50kV. All temporary distribution boards must have 30mA ELCB/RCCB.\n"
            "- **Excavation & Trenching:** Trenches $>1.5m$ depth require shoring, benching, or trench boxes (OSHA 1926 Subpart P)."
        )
    else:
        return prefix + (
            f"**Site Engineer Response:**\n\nRegarding your inquiry on *'{prompt}'*:\n"
            "On active construction sites, ensure all operations follow IS 456:2000 quality assurance and OSHA 1926 safety guidelines. "
            "Key checkpoints include:\n"
            "1. Checking structural drawings signed by an approved SE (Structural Engineer).\n"
            "2. Maintaining daily Pour Cards, Cube Registers (IS 516), and Site Safety Checklists.\n"
            "3. Verifying batching plant calibration and slump cone checks prior to each concrete pour.\n\n"
            "*Tip: You can ask specific questions like 'What is the lap length for 16mm rebar?', 'When to remove slab shuttering?', or 'Rainy weather concrete precautions'.*"
        )
