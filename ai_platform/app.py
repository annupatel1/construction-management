"""
app.py - Multimodal Construction Intelligence Platform
Skill Wallet Capstone Project 1 | Platform ID: 6ac4c98d1dcde6d7857c1642
Zero external database dependencies | 1-Click Streamlit Cloud Deployable
"""

import streamlit as st
import json
import os
from PIL import Image

from ai_engine import analyze_ppe_safety, analyze_concrete_crack, get_chatbot_response
from planning_engine import calculate_project_metrics, STRUCTURE_BENCHMARKS
from weather_engine import get_weather_advisory, WEATHER_PROTOCOLS

# ---------------------------------------------------------
# PAGE CONFIGURATION & STYLING
# ---------------------------------------------------------

st.set_page_config(
    page_title="Multimodal Construction Intelligence Platform",
    page_icon="🏗️",
    layout="wide",
    initial_sidebar_state="expanded",
)

# Custom Enterprise Construction Theme CSS
st.markdown("""
<style>
    /* Global styling */
    .main {
        background-color: #0d1117;
        color: #e6edf3;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }
    
    /* Headers */
    h1, h2, h3 {
        color: #f0f6fc;
        font-weight: 700;
        letter-spacing: -0.5px;
    }
    
    /* Top banner */
    .hero-banner {
        background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
        border: 1px solid #334155;
        border-radius: 12px;
        padding: 24px;
        margin-bottom: 24px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    }
    
    .hero-badge {
        display: inline-block;
        background-color: #f59e0b;
        color: #000;
        font-weight: 700;
        font-size: 0.75rem;
        padding: 4px 10px;
        border-radius: 9999px;
        text-transform: uppercase;
        margin-bottom: 8px;
    }
    
    /* Metric Cards */
    .metric-card {
        background: #161b22;
        border: 1px solid #30363d;
        border-radius: 10px;
        padding: 16px;
        text-align: center;
        transition: transform 0.2s ease, border-color 0.2s ease;
    }
    .metric-card:hover {
        border-color: #58a6ff;
        transform: translateY(-2px);
    }
    .metric-value {
        font-size: 1.8rem;
        font-weight: 800;
        color: #58a6ff;
        margin-top: 4px;
    }
    .metric-label {
        font-size: 0.82rem;
        color: #8b949e;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }
    
    /* Severity Badges */
    .badge-critical {
        background-color: #ef4444;
        color: white;
        padding: 3px 8px;
        border-radius: 6px;
        font-weight: bold;
        font-size: 0.75rem;
    }
    .badge-high {
        background-color: #ea580c;
        color: white;
        padding: 3px 8px;
        border-radius: 6px;
        font-weight: bold;
        font-size: 0.75rem;
    }
    .badge-medium {
        background-color: #f59e0b;
        color: black;
        padding: 3px 8px;
        border-radius: 6px;
        font-weight: bold;
        font-size: 0.75rem;
    }
    .badge-low {
        background-color: #10b981;
        color: white;
        padding: 3px 8px;
        border-radius: 6px;
        font-weight: bold;
        font-size: 0.75rem;
    }
    
    /* Protocol Cards */
    .protocol-card {
        background: #1c2128;
        border-left: 4px solid #58a6ff;
        border-radius: 0 8px 8px 0;
        padding: 14px 18px;
        margin-bottom: 12px;
    }
    
    /* Chat message container */
    .chat-bubble-user {
        background-color: #1e3a5f;
        border: 1px solid #2d5a88;
        border-radius: 12px 12px 2px 12px;
        padding: 12px 16px;
        margin: 8px 0;
        max-width: 85%;
        margin-left: auto;
    }
    .chat-bubble-bot {
        background-color: #161b22;
        border: 1px solid #30363d;
        border-radius: 12px 12px 12px 2px;
        padding: 14px 18px;
        margin: 8px 0;
        max-width: 85%;
    }
</style>
""", unsafe_allow_html=True)


# ---------------------------------------------------------
# SIDEBAR: CONFIGURATION, ARCHITECTURE & EPICS
# ---------------------------------------------------------

with st.sidebar:
    st.image("https://images.unsplash.com/photo-1541888946425-d0fbb186156f?w=400&auto=format&fit=crop&q=60", use_container_width=True)
    st.markdown("### 🏗️ CivicPulse AI")
    st.markdown("**Multimodal Construction Intelligence**")
    st.caption("Capstone Project 1 | Platform ID: `6ac4c98d1dcde6d7857c1642`")
    
    st.markdown("---")
    st.subheader("🔑 AI Model Gateway")
    gemini_key_input = st.text_input(
        "Google Gemini API Key",
        type="password",
        placeholder="AIzaSy...",
        help="Enter your Gemini 1.5 API key. If left blank, platform seamlessly runs in High-Fidelity Demonstration Mode."
    )
    
    if gemini_key_input:
        st.success("⚡ Gemini 1.5 Multimodal Connected")
    else:
        st.info("🛡️ Demonstration Mode Active\n(Zero-config fallback with OSHA & IS 456 datasets)")
        
    st.markdown("---")
    st.subheader("📐 Architecture & Standards")
    st.markdown("""
    - **Vision LLM:** Gemini 1.5 Flash / Pro
    - **Safety Standard:** OSHA 1926 CFR
    - **Concrete Code:** IS 456:2000 & ACI 224R
    - **Building Code:** NBC India 2016
    - **Generative 3D:** Pollinations Flux AI
    """)
    
    st.markdown("---")
    st.subheader("📋 Kanban Epics Coverage")
    st.markdown("""
    - **Pre-req:** Python 3.9+, Node, FastAPI
    - **Epic 1:** Requirements & Environment
    - **Epic 2:** PPE Safety Vision & Crack Diagnostics
    - **Epic 3:** Planning & Real Estate Valuation
    - **Epic 4:** Multimodal AI & JSON Contracts
    - **Epic 5:** Site Chatbot & Weather Advisory
    - **Epic 6:** Testing & Cloud Deployment
    """)
    
    st.markdown("---")
    st.caption("v1.0.0-Production | Built for 1-Click Cloud Deployment")


# ---------------------------------------------------------
# HEADER HERO BANNER
# ---------------------------------------------------------

st.markdown("""
<div class="hero-banner">
    <span class="hero-badge">AI Decision Support Platform</span>
    <h2>🏗️ Multimodal Construction Intelligence Platform</h2>
    <p style="color: #94a3b8; margin: 0; font-size: 0.95rem;">
        Site safety audits under OSHA 1926 & IS 456, concrete crack forensic diagnostics, 
        project estimation in INR, dynamic weather advisory, and civil engineering AI assistant.
    </p>
</div>
""", unsafe_allow_html=True)


# ---------------------------------------------------------
# TABS INTERFACE
# ---------------------------------------------------------

tab1, tab2, tab3, tab4 = st.tabs([
    "🦺 1. Site Safety & PPE Audit",
    "🔬 2. Crack Diagnostics & Integrity",
    "📊 3. Planning & Real Estate Valuation",
    "🌦️ 4. Daily Site Assistant & Weather"
])


# =========================================================
# TAB 1: SITE SAFETY & PPE AUDIT (EPIC 2)
# =========================================================
with tab1:
    st.markdown("### 🦺 Site Safety & Worker PPE Vision Audit")
    st.markdown("Automated compliance auditing for Hard Hats, High-Vis Vests, Safety Boots, and Fall Protection under **OSHA 1926** and **IS 456 / IS 15848**.")
    
    col_upload, col_preview = st.columns([1, 1])
    
    with col_upload:
        uploaded_site_file = st.file_uploader(
            "Upload Construction Site Photo (JPG / PNG)",
            type=["jpg", "jpeg", "png"],
            key="ppe_uploader"
        )
        
        use_sample_site = st.button("📁 Load Benchmark Site Inspection Photo", key="btn_sample_site")
        
        target_site_image = None
        if uploaded_site_file:
            target_site_image = Image.open(uploaded_site_file)
        elif use_sample_site or os.path.exists("samples/sample_site_photo.jpg"):
            if os.path.exists("samples/sample_site_photo.jpg"):
                target_site_image = Image.open("samples/sample_site_photo.jpg")

    with col_preview:
        if target_site_image:
            st.image(target_site_image, caption="Site Inspection Photo for AI Vision Audit", use_container_width=True)
        else:
            st.info("Please upload a photo or click 'Load Benchmark Site Inspection Photo' to begin.")

    if target_site_image:
        if st.button("🚀 Run Multimodal Safety Audit", type="primary", key="btn_run_ppe"):
            with st.spinner("Analyzing worker PPE compliance and hazard zones..."):
                audit_result = analyze_ppe_safety(target_site_image, api_key=gemini_key_input)
                st.session_state["ppe_result"] = audit_result

    # Render results
    if "ppe_result" in st.session_state:
        res = st.session_state["ppe_result"]
        st.markdown("---")
        st.markdown(f"#### 📋 Safety Audit Results <span style='font-size:0.8rem; color:#38bdf8'>({res.get('mode', 'MODE')})</span>", unsafe_allow_html=True)
        
        # Metric Cards
        score = res.get("safety_score", 0)
        status = res.get("overall_status", "Unknown")
        workers = res.get("workers_detected", 0)
        
        score_color = "#10b981" if score >= 85 else ("#f59e0b" if score >= 60 else "#ef4444")
        
        m1, m2, m3 = st.columns(3)
        with m1:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Safety Compliance Score</div>
                <div class="metric-value" style="color: {score_color};">{score}/100</div>
            </div>
            """, unsafe_allow_html=True)
        with m2:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Overall Site Status</div>
                <div class="metric-value" style="color: #f59e0b; font-size: 1.4rem;">{status}</div>
            </div>
            """, unsafe_allow_html=True)
        with m3:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Workers Audited</div>
                <div class="metric-value" style="color: #38bdf8;">{workers} Active</div>
            </div>
            """, unsafe_allow_html=True)
            
        st.markdown("<br>", unsafe_allow_html=True)
        
        # PPE Checklist Table
        st.subheader("🛡️ Personal Protective Equipment (PPE) Checklist")
        ppe_items = res.get("ppe_detection", [])
        if ppe_items:
            for item in ppe_items:
                st_color = "#10b981" if item["status"] == "Detected" else ("#f59e0b" if item["status"] == "Partial" else "#ef4444")
                st.markdown(f"""
                <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 12px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center;">
                    <div style="flex: 2;">
                        <strong>{item['item']}</strong><br>
                        <span style="color: #8b949e; font-size: 0.85rem;">{item.get('observations', '')}</span>
                    </div>
                    <div style="flex: 1; text-align: center;">
                        <span style="color: #8b949e; font-size: 0.85rem;">Compliance: </span><strong>{item.get('compliance_rate', 'N/A')}</strong>
                    </div>
                    <div style="flex: 1; text-align: right;">
                        <span style="background-color: {st_color}; color: white; padding: 4px 10px; border-radius: 9999px; font-weight: bold; font-size: 0.8rem;">
                            {item['status']}
                        </span>
                    </div>
                </div>
                """, unsafe_allow_html=True)
                
        # Hazards Identified
        st.subheader("⚠️ Physical Site Hazards & Corrective Actions")
        hazards = res.get("hazards_identified", [])
        if hazards:
            for h in hazards:
                sev = h.get("severity", "Medium")
                badge_class = f"badge-{sev.lower()}" if sev.lower() in ["critical", "high", "medium", "low"] else "badge-medium"
                
                st.markdown(f"""
                <div class="protocol-card" style="border-left-color: {'#ef4444' if sev == 'Critical' else '#f59e0b'};">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                        <span style="font-weight: 700; font-size: 1.05rem;">{h.get('title')}</span>
                        <span class="{badge_class}">{sev} SEVERITY</span>
                    </div>
                    <div style="color: #94a3b8; font-size: 0.88rem; margin-bottom: 4px;">
                        📍 <strong>Location:</strong> {h.get('location', 'Site Area')} &nbsp;|&nbsp; 
                        📜 <strong>Standard:</strong> {h.get('standard_violated', 'OSHA 1926')}
                    </div>
                    <div style="background: #0d1117; border-radius: 6px; padding: 8px 12px; font-size: 0.9rem; color: #38bdf8; margin-top: 6px;">
                        🛠️ <strong>Corrective Remedy:</strong> {h.get('corrective_remedy')}
                    </div>
                </div>
                """, unsafe_allow_html=True)
                
        # Auditor Summary
        if "auditor_summary" in res:
            st.info(f"**Auditor Sign-off Summary:** {res['auditor_summary']}")
            
        # Downloadable JSON report
        json_report = json.dumps(res, indent=2)
        st.download_button(
            label="📥 Download Structured JSON Safety Report",
            data=json_report,
            file_name="site_safety_audit_report.json",
            mime="application/json"
        )


# =========================================================
# TAB 2: CRACK DIAGNOSTICS & STRUCTURAL INTEGRITY (EPIC 2)
# =========================================================
with tab2:
    st.markdown("### 🔬 Concrete Crack Diagnostics & Structural Integrity")
    st.markdown("Forensic fracture analysis classifying defect type, width (mm), root causes, and engineering repairs under **IS 456:2000 Cl. 35.3.2** and **ACI 224R-01**.")
    
    c_upload, c_preview = st.columns([1, 1])
    
    with c_upload:
        uploaded_crack_file = st.file_uploader(
            "Upload Concrete / Masonry Surface Photo (JPG / PNG)",
            type=["jpg", "jpeg", "png"],
            key="crack_uploader"
        )
        use_sample_crack = st.button("📁 Load Benchmark Structural Fracture Photo", key="btn_sample_crack")
        
        target_crack_image = None
        if uploaded_crack_file:
            target_crack_image = Image.open(uploaded_crack_file)
        elif use_sample_crack or os.path.exists("samples/sample_concrete_crack.jpg"):
            if os.path.exists("samples/sample_concrete_crack.jpg"):
                target_crack_image = Image.open("samples/sample_concrete_crack.jpg")
                
    with c_preview:
        if target_crack_image:
            st.image(target_crack_image, caption="Concrete Surface for Forensic Diagnostic Scan", use_container_width=True)
        else:
            st.info("Upload a concrete crack image or click 'Load Benchmark Structural Fracture Photo'.")
            
    if target_crack_image:
        if st.button("🔍 Run Forensic Crack Diagnosis", type="primary", key="btn_run_crack"):
            with st.spinner("Classifying crack pattern, measuring aperture, and compiling repair specs..."):
                crack_res = analyze_concrete_crack(target_crack_image, api_key=gemini_key_input)
                st.session_state["crack_result"] = crack_res

    # Render crack results
    if "crack_result" in st.session_state:
        cr = st.session_state["crack_result"]
        st.markdown("---")
        st.markdown(f"#### 🧪 Forensic Diagnostic Report <span style='font-size:0.8rem; color:#38bdf8'>({cr.get('mode', 'MODE')})</span>", unsafe_allow_html=True)
        
        cm1, cm2, cm3, cm4 = st.columns(4)
        with cm1:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Defect Classification</div>
                <div class="metric-value" style="font-size: 1.3rem;">{cr.get('defect_type', 'N/A')}</div>
            </div>
            """, unsafe_allow_html=True)
        with cm2:
            width = cr.get("estimated_width_mm", 0.0)
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Estimated Width</div>
                <div class="metric-value" style="color: #ea580c;">{width} mm</div>
            </div>
            """, unsafe_allow_html=True)
        with cm3:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Severity Level</div>
                <div class="metric-value" style="color: #ef4444; font-size: 1.3rem;">{cr.get('severity', 'N/A')}</div>
            </div>
            """, unsafe_allow_html=True)
        with cm4:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Action Urgency</div>
                <div class="metric-value" style="color: #f59e0b; font-size: 1.0rem;">{cr.get('urgency', 'Immediate')}</div>
            </div>
            """, unsafe_allow_html=True)
            
        st.markdown("<br>", unsafe_allow_html=True)
        
        # IS 456 Code Check Alert
        code_check = cr.get("code_limit_reference", "")
        st.warning(f"⚖️ **IS 456:2000 Compliance Assessment:** {code_check}")
        
        # Root Causes
        st.subheader("🔍 Identified Root Causes")
        causes = cr.get("root_causes", [])
        for c in causes:
            st.markdown(f"- 🔸 **{c}**")
            
        # Recommended Engineering Repairs
        st.subheader("🛠️ Recommended Engineering Repair Protocol")
        repairs = cr.get("recommended_repairs", [])
        for r in repairs:
            st.markdown(f"""
            <div class="protocol-card" style="border-left-color: #38bdf8;">
                <div style="font-weight: 700; color: #f0f6fc; font-size: 1rem;">
                    Step {r.get('step', 1)}: {r.get('method')}
                </div>
                <div style="color: #94a3b8; font-size: 0.88rem; margin: 4px 0;">
                    🧪 <strong>Materials:</strong> {r.get('materials')}
                </div>
                <div style="color: #38bdf8; font-size: 0.85rem;">
                    📋 <strong>Execution Spec:</strong> {r.get('specification')}
                </div>
            </div>
            """, unsafe_allow_html=True)
            
        # Structural Engineer Notes
        if "engineer_notes" in cr:
            st.info(f"**Forensic Engineer Notes:** {cr['engineer_notes']}")
            
        crack_json = json.dumps(cr, indent=2)
        st.download_button(
            label="📥 Download Forensic Crack Diagnostic JSON",
            data=crack_json,
            file_name="concrete_crack_diagnostic_report.json",
            mime="application/json"
        )


# =========================================================
# TAB 3: PLANNING & REAL ESTATE VALUATION (EPIC 3)
# =========================================================
with tab3:
    st.markdown("### 📊 Project Planning, Estimation & Real Estate Valuation")
    st.markdown("Calculate total project budget, CapEx incurred, current asset valuation in **INR (Lakhs / Crores)**, steel/cement consumption, and 3D architectural rendering.")
    
    col_inputs, col_results = st.columns([1, 1.2])
    
    with col_inputs:
        st.markdown("#### ⚙️ Project Parameters")
        
        structure_type = st.selectbox(
            "Structure Type",
            list(STRUCTURE_BENCHMARKS.keys()),
            index=0
        )
        
        built_up_sqft = st.number_input(
            "Built-Up Area (sq. ft.)",
            min_value=1000,
            max_value=2000000,
            value=25000,
            step=2500,
            help="Total gross construction floor area."
        )
        
        location_tier = st.selectbox(
            "Location Tier (Indian Real Estate)",
            [
                "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)",
                "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)",
                "Tier 3 (Emerging Urban Centers)"
            ],
            index=0
        )
        
        completion_percent = st.slider(
            "Construction Completion (%)",
            min_value=0,
            max_value=100,
            value=45,
            step=5,
            help="Current site milestone progress."
        )
        
        st.caption(f"**Structural Profile:** {STRUCTURE_BENCHMARKS[structure_type]['description']}")

    # Calculate metrics
    metrics = calculate_project_metrics(
        structure_type=structure_type,
        built_up_sqft=built_up_sqft,
        location_tier=location_tier,
        completion_percentage=completion_percent
    )
    
    with col_results:
        st.markdown("#### 💰 Financial Valuation & CapEx (INR)")
        
        f1, f2 = st.columns(2)
        with f1:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">Total Construction Budget</div>
                <div class="metric-value" style="color: #38bdf8;">{metrics['total_budget_formatted']}</div>
            </div>
            """, unsafe_allow_html=True)
            
            st.markdown(f"""
            <div class="metric-card" style="margin-top: 10px;">
                <div class="metric-label">Current Asset WIP Valuation</div>
                <div class="metric-value" style="color: #10b981;">{metrics['current_asset_valuation_formatted']}</div>
            </div>
            """, unsafe_allow_html=True)
            
        with f2:
            st.markdown(f"""
            <div class="metric-card">
                <div class="metric-label">CapEx Incurred to Date</div>
                <div class="metric-value" style="color: #f59e0b;">{metrics['capex_incurred_formatted']}</div>
            </div>
            """, unsafe_allow_html=True)
            
            st.markdown(f"""
            <div class="metric-card" style="margin-top: 10px;">
                <div class="metric-label">Full Completed Asset Value</div>
                <div class="metric-value" style="color: #a855f7;">{metrics['full_market_valuation_formatted']}</div>
            </div>
            """, unsafe_allow_html=True)

    st.markdown("---")
    
    # Milestone Progress
    st.subheader("🏗️ Milestone Execution Tracker")
    st.progress(completion_percent / 100.0)
    st.markdown(f"**Current Milestone:** `{metrics['milestone']['current_phase']}` ({completion_percent}%)")
    st.caption(f"Status: {metrics['milestone']['phase_status']}")
    
    # Material Requirements
    st.subheader("📦 Bill of Materials (BOM) Estimation")
    m_mat1, m_mat2, m_mat3, m_mat4 = st.columns(4)
    with m_mat1:
        st.markdown(f"""
        <div class="metric-card">
            <div class="metric-label">Structural Steel</div>
            <div class="metric-value" style="font-size: 1.4rem;">{metrics['materials']['structural_steel_tonnes']} MT</div>
        </div>
        """, unsafe_allow_html=True)
    with m_mat2:
        st.markdown(f"""
        <div class="metric-card">
            <div class="metric-label">Cement (50kg Bags)</div>
            <div class="metric-value" style="font-size: 1.4rem; color: #10b981;">{metrics['materials']['cement_bags_50kg']:,}</div>
        </div>
        """, unsafe_allow_html=True)
    with m_mat3:
        st.markdown(f"""
        <div class="metric-card">
            <div class="metric-label">M-Sand / River Sand</div>
            <div class="metric-value" style="font-size: 1.4rem; color: #f59e0b;">{metrics['materials']['river_m_sand_cft']:,} cu ft</div>
        </div>
        """, unsafe_allow_html=True)
    with m_mat4:
        st.markdown(f"""
        <div class="metric-card">
            <div class="metric-label">Coarse Aggregates</div>
            <div class="metric-value" style="font-size: 1.4rem; color: #a855f7;">{metrics['materials']['coarse_aggregates_cft']:,} cu ft</div>
        </div>
        """, unsafe_allow_html=True)
        
    st.markdown("---")
    
    # Architectural Visualization (Pollinations AI)
    st.subheader("🎨 Conceptual 3D Architectural Rendering")
    st.caption("AI-generated exterior visualization synchronized with project typology and location.")
    
    render_col1, render_col2 = st.columns([1.5, 1])
    with render_col1:
        st.image(
            metrics["visualization"]["image_url"],
            caption=f"Conceptual Architectural Visualization: {structure_type} ({location_tier})",
            use_container_width=True
        )
    with render_col2:
        st.markdown(f"""
        <div style="background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 16px;">
            <h5 style="color: #38bdf8; margin-top: 0;">🎨 Flux AI Prompt Generator</h5>
            <p style="color: #94a3b8; font-size: 0.85rem; font-family: monospace;">
                "{metrics['visualization']['prompt']}"
            </p>
            <hr style="border-color: #30363d;">
            <div style="font-size: 0.85rem; color: #e6edf3;">
                <strong>Engine:</strong> Pollinations AI / Flux Diffusion<br>
                <strong>Resolution:</strong> 1024x640 Octane Cinematic<br>
                <strong>License:</strong> Royalty Free Architectural Concept
            </div>
        </div>
        """, unsafe_allow_html=True)


# =========================================================
# TAB 4: DAILY SITE ASSISTANT & WEATHER-AWARE ADVISORY (EPIC 5)
# =========================================================
with tab4:
    st.markdown("### 🌦️ Daily Site Assistant & Weather-Aware Advisory")
    st.markdown("Dynamic OSHA 1926 & IS 456 meteorological safety protocols, paired with a multi-turn civil engineering assistant chatbot.")
    
    # Weather Selector
    weather_condition = st.selectbox(
        "Current Site Weather Condition",
        list(WEATHER_PROTOCOLS.keys()),
        index=0
    )
    
    advisory = get_weather_advisory(weather_condition)
    
    # Weather Banner
    st.markdown(f"""
    <div style="background: #161b22; border-left: 6px solid {advisory['severity_color']}; border-radius: 8px; padding: 18px; margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h4 style="margin: 0; color: #f0f6fc;">{advisory['icon']} {weather_condition}</h4>
            <span style="background-color: {advisory['severity_color']}; color: #000; font-weight: bold; font-size: 0.8rem; padding: 4px 10px; border-radius: 9999px;">
                STATUS: {advisory['status_level']}
            </span>
        </div>
        <p style="color: #cbd5e1; margin-top: 8px; margin-bottom: 0;">{advisory['summary']}</p>
    </div>
    """, unsafe_allow_html=True)
    
    col_proto, col_conc = st.columns([1.2, 1])
    
    with col_proto:
        st.subheader("⚡ Mandatory Safety Directives")
        for alert in advisory["mandatory_alerts"]:
            st.markdown(f"""
            <div class="protocol-card" style="border-left-color: {advisory['severity_color']};">
                <div style="font-weight: 700; color: #f0f6fc;">{alert['title']}</div>
                <div style="color: #8b949e; font-size: 0.82rem;">Standard: {alert['standard']}</div>
                <div style="color: #e6edf3; font-size: 0.9rem; margin-top: 4px;">{alert['action']}</div>
            </div>
            """, unsafe_allow_html=True)
            
    with col_conc:
        st.subheader("🧊 Concrete Curing & Pouring Protocol")
        st.markdown(f"""
        <div style="background: #0d1117; border: 1px solid #30363d; border-radius: 8px; padding: 16px;">
            <h5 style="color: #38bdf8; margin-top: 0;">IS 456 / IS 7861 Directive</h5>
            <p style="color: #cbd5e1; font-size: 0.92rem; line-height: 1.5;">
                {advisory['concrete_advisory']}
            </p>
        </div>
        """, unsafe_allow_html=True)

    st.markdown("---")
    
    # Engineering Assistant Chatbot
    st.subheader("💬 CivicPulse Engineering Chatbot")
    st.caption("Ask questions regarding IS 456, OSHA 1926, rebar detailing, formwork stripping, or mix designs.")
    
    # Session state for chat history
    if "chat_history" not in st.session_state:
        st.session_state["chat_history"] = [
            {"role": "assistant", "content": "Hello Engineer! I am CivicPulse AI, your on-site civil and structural advisor. Ask me anything about IS 456 concrete codes, OSHA safety rules, or rebar schedules."}
        ]
        
    # Quick prompt suggestion buttons
    q_col1, q_col2, q_col3 = st.columns(3)
    with q_col1:
        if st.button("⏱️ Striking period for slab shuttering?"):
            st.session_state["user_prompt_input"] = "When can we remove formwork/props for a 5m slab under IS 456?"
    with q_col2:
        if st.button("💧 Hot weather concrete curing rules?"):
            st.session_state["user_prompt_input"] = "What is the mandatory curing period for concrete in 42°C heat per IS 456?"
    with q_col3:
        if st.button("🔗 Rebar lap length for M25 beam?"):
            st.session_state["user_prompt_input"] = "What is the development and lap length for 20mm Fe500D rebar in M25 concrete?"

    # Display chat history
    chat_container = st.container()
    with chat_container:
        for msg in st.session_state["chat_history"]:
            if msg["role"] == "user":
                st.markdown(f"""
                <div class="chat-bubble-user">
                    <strong>👷 Project Engineer:</strong><br>{msg['content']}
                </div>
                """, unsafe_allow_html=True)
            else:
                st.markdown(f"""
                <div class="chat-bubble-bot">
                    <strong>🤖 CivicPulse AI (Civil Advisor):</strong><br>{msg['content']}
                </div>
                """, unsafe_allow_html=True)

    # Chat Input
    default_text = st.session_state.pop("user_prompt_input", "")
    with st.form("chat_form", clear_on_submit=True):
        user_input = st.text_input("Enter your engineering query:", value=default_text)
        submit_chat = st.form_submit_button("Send Query 🚀")
        
        if submit_chat and user_input:
            st.session_state["chat_history"].append({"role": "user", "content": user_input})
            with st.spinner("CivicPulse AI evaluating engineering specifications..."):
                reply = get_chatbot_response(
                    prompt=user_input,
                    history=st.session_state["chat_history"],
                    api_key=gemini_key_input
                )
                st.session_state["chat_history"].append({"role": "assistant", "content": reply})
                st.rerun()
