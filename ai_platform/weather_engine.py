"""
weather_engine.py - Weather-Aware Construction Site Advisory & Protocols
OSHA 1926 & IS 456 / IS 7861 Hot/Cold Weather Concreting Guidelines.
"""

from typing import Dict, Any, List

WEATHER_PROTOCOLS = {
    "High Wind (Gale / Gusts > 35 km/h)": {
        "status_level": "WARNING",
        "severity_color": "#f59e0b",
        "icon": "💨",
        "summary": "Wind speeds exceed safe operating thresholds for vertical transport and elevated activities.",
        "mandatory_alerts": [
            {
                "title": "Tower Crane & Hoist Suspension",
                "standard": "OSHA 1926.1431 / IS 13367",
                "action": "Immediate suspension of tower crane hoisting, mobile crane booms, and exterior material hoists when sustained wind speeds exceed 38 km/h (20 knots). Lock crane in free-wheeling (weathervaning) mode."
            },
            {
                "title": "Suspended Scaffolding & Cradle Ban",
                "standard": "OSHA 1926.451 / IS 3696",
                "action": "Cease all swing-stage and suspended facade cradles. Anchor loose scaffolding planks and remove vertical debris netting if wind force stresses the scaffold tie-ins."
            },
            {
                "title": "Loose Material Tie-Down",
                "standard": "NBC 2016 Part 7",
                "action": "Fasten or weight down all roof sheeting, plywood formwork panels, GI sheets, and insulation boards on elevated decks."
            }
        ],
        "concrete_advisory": "High winds accelerate surface moisture evaporation on freshly placed concrete. Apply aliphatic alcohol evaporation retarders or polythene sheets immediately after screeding to prevent plastic shrinkage cracking (IS 456 Cl. 13.5)."
    },
    "Heavy Rain & Monsoonal Storms": {
        "status_level": "CRITICAL",
        "severity_color": "#ef4444",
        "icon": "⛈️",
        "summary": "High precipitation increases trench cave-in risks, electrical ground faults, and concrete washout.",
        "mandatory_alerts": [
            {
                "title": "Excavation & Trench Flood Evacuation",
                "standard": "OSHA 1926.651 / IS 3764",
                "action": "Evacuate all deep trenches and basement pits immediately. Inspect shoring and soil saturation before any worker re-entry. Deploy 3-inch submersible dewatering pumps."
            },
            {
                "title": "Electrical Distribution Shut-Off",
                "standard": "OSHA 1926.404 / IS 732",
                "action": "Isolate all temporary outdoor distribution boards (DBs) and switchgears. Ensure all circuits are routed through 30mA earth-leakage circuit breakers (ELCB/RCCB)."
            },
            {
                "title": "Concreting Halt & Tarpaulin Protection",
                "standard": "IS 456:2000 Cl. 13.4",
                "action": "Do NOT cast exposed slabs during active rain. If an unexpected shower hits during a pour, cover the fresh slab with heavy-gauge waterproof tarpaulins and establish a cold-joint stop-end."
            }
        ],
        "concrete_advisory": "Rainwater mixing with unset concrete dilutes the water-cement ratio, drastically reducing compressive strength and inducing surface dusting. Slump must be re-checked before resumption."
    },
    "Extreme Heat & Solar Radiation (> 40°C)": {
        "status_level": "WARNING",
        "severity_color": "#ea580c",
        "icon": "☀️",
        "summary": "Extreme ambient heat causes thermal stress on workers and rapid hydration slump loss in concrete.",
        "mandatory_alerts": [
            {
                "title": "Heat Stress & Hydration Work-Rest Cycles",
                "standard": "OSHA Technical Manual (OTM) Sec. III Ch. 4 / Factories Act",
                "action": "Enforce mandatory 15-minute shaded rest breaks every 60 minutes for heavy labor. Provide chilled water with oral rehydration salts (ORS) at every floor level."
            },
            {
                "title": "Hot-Weather Concreting Protocol",
                "standard": "IS 7861 (Part 1): 1975 / ACI 305R",
                "action": "Pour concrete strictly during early morning (5:00 AM - 10:00 AM) or night shifts. Use chilled batching water or flaked ice to keep fresh concrete delivery temperature below 32°C."
            },
            {
                "title": "Intensive Continuous Moist Curing",
                "standard": "IS 456:2000 Cl. 13.5.1",
                "action": "Begin continuous wet burlap or ponding curing immediately after initial set. Curing period must be extended to minimum 10-14 days to prevent thermal micro-cracking."
            }
        ],
        "concrete_advisory": "Accelerated cement hydration leads to rapid workability loss, higher early strength but reduced 28-day ultimate strength. Add retarder admixtures (ASTM C494 Type D / IS 9103)."
    },
    "Normal Fair-Weather Conditions (22°C - 30°C)": {
        "status_level": "OPTIMAL",
        "severity_color": "#10b981",
        "icon": "🌤️",
        "summary": "Ideal ambient conditions for full-scale civil, structural, finishing, and crane operations.",
        "mandatory_alerts": [
            {
                "title": "Full Velocity Site Operations",
                "standard": "General OSHA 1926 / NBC 2016",
                "action": "Standard operating protocols active. Ensure daily morning Tool-Box Talks (TBT) and valid daily hot-work / height permits are logged."
            },
            {
                "title": "Routine Perimeter & Scaffolding Check",
                "standard": "IS 3696 / OSHA 1926.451",
                "action": "Verify toe-boards, handrails, and catch nets on leading edges and facade perimeters."
            }
        ],
        "concrete_advisory": "Optimal curing conditions. Maintain standard 7-day moist curing for OPC concrete (IS 456 Cl. 13.5)."
    }
}


def get_weather_advisory(condition_key: str) -> Dict[str, Any]:
    """Returns dynamic safety rules and concrete engineering protocols for given weather."""
    default_key = "Normal Fair-Weather Conditions (22°C - 30°C)"
    return WEATHER_PROTOCOLS.get(condition_key, WEATHER_PROTOCOLS[default_key])
