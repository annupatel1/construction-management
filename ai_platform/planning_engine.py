"""
planning_engine.py - Project Planning, Estimation, Real Estate Valuation & 3D Renderings
Calculates construction economics in INR (Lakhs and Crores) and material quantities.
"""

from typing import Dict, Any
import urllib.parse

# Benchmark baseline rates per sqft for construction (INR/sqft) in Indian Real Estate
STRUCTURE_BENCHMARKS = {
    "Residential High-Rise (RCC G+20)": {
        "base_cost_sqft": 2400,
        "steel_kg_per_sqft": 4.5,      # 4.5 kg steel per sqft
        "cement_bags_per_sqft": 0.42,  # 0.42 bags (50kg) per sqft
        "sand_cft_per_sqft": 1.75,     # 1.75 cu ft sand
        "aggregate_cft_per_sqft": 2.2, # 2.2 cu ft aggregate
        "market_rate_multiplier": {
            "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)": 14500,  # Market selling rate INR/sqft
            "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)": 8200,
            "Tier 3 (Emerging Urban Centers)": 4800,
        },
        "description": "Multi-tier RCC shear wall structure, high-speed elevators, seismic Zone III/IV compliant."
    },
    "Commercial Grade-A Office Complex": {
        "base_cost_sqft": 3200,
        "steel_kg_per_sqft": 5.8,
        "cement_bags_per_sqft": 0.50,
        "sand_cft_per_sqft": 1.9,
        "aggregate_cft_per_sqft": 2.5,
        "market_rate_multiplier": {
            "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)": 18500,
            "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)": 10500,
            "Tier 3 (Emerging Urban Centers)": 6200,
        },
        "description": "Curtain-glazed facade, central HVAC plant, large column-free spans, double basements."
    },
    "Industrial Pre-Engineered Building (PEB)": {
        "base_cost_sqft": 1600,
        "steel_kg_per_sqft": 3.8,
        "cement_bags_per_sqft": 0.28,
        "sand_cft_per_sqft": 1.2,
        "aggregate_cft_per_sqft": 1.6,
        "market_rate_multiplier": {
            "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)": 6500,
            "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)": 4200,
            "Tier 3 (Emerging Urban Centers)": 2900,
        },
        "description": "Heavy-duty structural portal frames, Galvalume insulated roof panels, industrial flooring (FM2)."
    },
    "Luxury Villa / Low-Rise G+2": {
        "base_cost_sqft": 2900,
        "steel_kg_per_sqft": 4.0,
        "cement_bags_per_sqft": 0.45,
        "sand_cft_per_sqft": 1.8,
        "aggregate_cft_per_sqft": 2.1,
        "market_rate_multiplier": {
            "Tier 1 (Mumbai, Delhi-NCR, Bengaluru)": 16000,
            "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)": 9500,
            "Tier 3 (Emerging Urban Centers)": 5500,
        },
        "description": "Premium monolithic RCC frame, Italian marble finishes, VRV air-conditioning, landscaped terrace."
    }
}


def format_inr(amount: float) -> str:
    """Format numeric INR value into Indian Lakhs or Crores representation."""
    if amount >= 10_000_000:
        cr = amount / 10_000_000
        return f"₹ {cr:,.2f} Cr"
    elif amount >= 100_000:
        lakh = amount / 100_000
        return f"₹ {lakh:,.2f} Lakh"
    else:
        return f"₹ {amount:,.2f}"


def calculate_project_metrics(
    structure_type: str,
    built_up_sqft: float,
    location_tier: str,
    completion_percentage: float
) -> Dict[str, Any]:
    """
    Computes CapEx, Real Estate Valuation, Material Requirements, and Milestone Stages.
    """
    spec = STRUCTURE_BENCHMARKS.get(
        structure_type, 
        STRUCTURE_BENCHMARKS["Residential High-Rise (RCC G+20)"]
    )
    
    # Cost calculations
    base_cost_per_sqft = spec["base_cost_sqft"]
    total_budget_inr = built_up_sqft * base_cost_per_sqft
    capex_incurred_inr = total_budget_inr * (completion_percentage / 100.0)
    
    # Valuation calculation
    market_rate_per_sqft = spec["market_rate_multiplier"].get(
        location_tier, 10000
    )
    full_completed_market_value = built_up_sqft * market_rate_per_sqft
    
    # Current WIP Valuation: Base land & completed value with discount for incomplete construction
    # Land/Permits baseline is approx 35% of total project value
    base_land_entitlement_fraction = 0.35
    wip_completion_fraction = (completion_percentage / 100.0) * (1 - base_land_entitlement_fraction)
    current_asset_valuation_inr = full_completed_market_value * (base_land_entitlement_fraction + wip_completion_fraction)
    
    # Materials calculations
    steel_tonnes = (built_up_sqft * spec["steel_kg_per_sqft"]) / 1000.0
    cement_bags = int(built_up_sqft * spec["cement_bags_per_sqft"])
    sand_cft = int(built_up_sqft * spec["sand_cft_per_sqft"])
    aggregate_cft = int(built_up_sqft * spec["aggregate_cft_per_sqft"])
    
    # Active milestone phase
    if completion_percentage <= 20:
        current_phase = "Phase 1: Substructure, Deep Piling & Foundation Footings"
        phase_status = "Piling & basement retaining wall construction underway"
    elif completion_percentage <= 55:
        current_phase = "Phase 2: Superstructure Reinforced Concrete Frame (RCC)"
        phase_status = "Floor casting, shear walls, and column casting at peak velocity"
    elif completion_percentage <= 85:
        current_phase = "Phase 3: Masonry Blockwork, MEP, Wet Services & Glazing"
        phase_status = "Internal plumbing, fire-fighting risers, and electrical conduits"
    else:
        current_phase = "Phase 4: Architectural Finishing, Commissioning & Occupancy NOC"
        phase_status = "Flooring, wall paint, elevator testing, and fire clearance"
        
    # Pollinations 3D architectural rendering generation
    pollinations_prompt = (
        f"hyperrealistic modern architectural 3D exterior render of {structure_type}, "
        f"located in {location_tier}, photorealistic 8k octane render, architectural photography, "
        f"golden hour lighting, cinematic luxury commercial construction project"
    )
    encoded_prompt = urllib.parse.quote(pollinations_prompt)
    render_image_url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width=1024&height=640&nologo=true&seed=42"
    
    return {
        "structure_type": structure_type,
        "built_up_sqft": built_up_sqft,
        "location_tier": location_tier,
        "completion_percentage": completion_percentage,
        "total_budget_inr": total_budget_inr,
        "total_budget_formatted": format_inr(total_budget_inr),
        "capex_incurred_inr": capex_incurred_inr,
        "capex_incurred_formatted": format_inr(capex_incurred_inr),
        "market_rate_per_sqft": market_rate_per_sqft,
        "current_asset_valuation_inr": current_asset_valuation_inr,
        "current_asset_valuation_formatted": format_inr(current_asset_valuation_inr),
        "full_market_valuation_inr": full_completed_market_value,
        "full_market_valuation_formatted": format_inr(full_completed_market_value),
        "materials": {
            "structural_steel_tonnes": round(steel_tonnes, 2),
            "cement_bags_50kg": cement_bags,
            "river_m_sand_cft": sand_cft,
            "coarse_aggregates_cft": aggregate_cft
        },
        "milestone": {
            "current_phase": current_phase,
            "phase_status": phase_status,
            "progress_percent": completion_percentage
        },
        "visualization": {
            "prompt": pollinations_prompt,
            "image_url": render_image_url
        }
    }
