"""
test_suite.py - Comprehensive Unit, Functional & Edge Case Test Suite
Tests all modules, Pydantic schemas, and FastAPI endpoints.
"""

import unittest
import io
from PIL import Image
from fastapi.testclient import TestClient

from backend_api import app
from ai_engine import analyze_ppe_safety, analyze_concrete_crack, get_chatbot_response
from planning_engine import calculate_project_metrics, format_inr
from weather_engine import get_weather_advisory, WEATHER_PROTOCOLS

client = TestClient(app)

class TestConstructionIntelligence(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        # Create an in-memory test image
        img = Image.new("RGB", (200, 200), color=(100, 150, 200))
        buf = io.BytesIO()
        img.save(buf, format="JPEG")
        cls.test_image_bytes = buf.getvalue()
        cls.test_image = img

    def test_health_check(self):
        """Test API health endpoint."""
        response = client.get("/api/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "healthy")
        self.assertEqual(data["platform_id"], "6ac4c98d1dcde6d7857c1642")

    def test_ppe_safety_audit(self):
        """Test PPE vision audit schema and contract."""
        res = analyze_ppe_safety(self.test_image)
        self.assertIn("safety_score", res)
        self.assertTrue(0 <= res["safety_score"] <= 100)
        self.assertIn("overall_status", res)
        self.assertIn("ppe_detection", res)
        self.assertIn("hazards_identified", res)
        self.assertIsInstance(res["hazards_identified"], list)

    def test_crack_diagnostics(self):
        """Test concrete crack analysis and IS 456 limits."""
        res = analyze_concrete_crack(self.test_image)
        self.assertIn("defect_type", res)
        self.assertIn("estimated_width_mm", res)
        self.assertIn("root_causes", res)
        self.assertIn("recommended_repairs", res)
        self.assertGreater(len(res["recommended_repairs"]), 0)

    def test_planning_and_valuation_calculations(self):
        """Test INR budgeting, CapEx, and BOM metrics."""
        metrics = calculate_project_metrics(
            structure_type="Residential High-Rise (RCC G+20)",
            built_up_sqft=20000,
            location_tier="Tier 1 (Mumbai, Delhi-NCR, Bengaluru)",
            completion_percentage=50.0
        )
        self.assertEqual(metrics["total_budget_inr"], 20000 * 2400)
        self.assertEqual(metrics["capex_incurred_inr"], (20000 * 2400) * 0.5)
        self.assertIn("Cr", metrics["total_budget_formatted"])
        self.assertIn("structural_steel_tonnes", metrics["materials"])
        self.assertIn("image_url", metrics["visualization"])
        self.assertTrue(metrics["visualization"]["image_url"].startswith("https://image.pollinations.ai"))

    def test_format_inr(self):
        """Test Indian currency unit formatting (Lakhs and Crores)."""
        self.assertEqual(format_inr(50_000_000), "₹ 5.00 Cr")
        self.assertEqual(format_inr(2_500_000), "₹ 25.00 Lakh")
        self.assertEqual(format_inr(50_000), "₹ 50,000.00")

    def test_weather_advisory_protocols(self):
        """Test dynamic weather protocols for all categories."""
        for condition in WEATHER_PROTOCOLS.keys():
            advisory = get_weather_advisory(condition)
            self.assertIn("status_level", advisory)
            self.assertIn("mandatory_alerts", advisory)
            self.assertIn("concrete_advisory", advisory)

    def test_chatbot_engineering_responses(self):
        """Test engineering chatbot domain answers."""
        reply_curing = get_chatbot_response("How many days should I cure concrete under IS 456?", [])
        self.assertIn("IS 456", reply_curing)
        
        reply_formwork = get_chatbot_response("Striking period for slab shuttering", [])
        self.assertIn("Striking Periods", reply_formwork)

    def test_fastapi_endpoints_integration(self):
        """Test FastAPI multipart endpoints and JSON contract validation."""
        # 1. PPE Audit endpoint
        response = client.post(
            "/api/ppe-audit",
            files={"file": ("site.jpg", self.test_image_bytes, "image/jpeg")},
            data={"api_key": ""}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("safety_score", data)

        # 2. Crack Diagnostics endpoint
        response = client.post(
            "/api/crack-diagnostics",
            files={"file": ("crack.jpg", self.test_image_bytes, "image/jpeg")},
            data={"api_key": ""}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("defect_type", data)

        # 3. Planning endpoint
        response = client.post(
            "/api/planning-valuation",
            json={
                "structure_type": "Commercial Grade-A Office Complex",
                "built_up_sqft": 15000,
                "location_tier": "Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)",
                "completion_percentage": 75.0
            }
        )
        self.assertEqual(response.status_code, 200)
        plan_data = response.json()
        self.assertIn("capex_incurred_formatted", plan_data)

        # 4. Chat endpoint
        response = client.post(
            "/api/chat",
            json={"prompt": "What is the lap length for Fe500D rebar?"}
        )
        self.assertEqual(response.status_code, 200)
        chat_data = response.json()
        self.assertIn("reply", chat_data)

    def test_edge_cases(self):
        """Test error handling with corrupted files and invalid parameters."""
        # Corrupted file upload
        response = client.post(
            "/api/ppe-audit",
            files={"file": ("bad.jpg", b"corrupted byte stream", "image/jpeg")},
        )
        self.assertEqual(response.status_code, 400)

        # Non-image mime type
        response = client.post(
            "/api/crack-diagnostics",
            files={"file": ("test.txt", b"plain text", "text/plain")},
        )
        self.assertEqual(response.status_code, 400)


if __name__ == "__main__":
    unittest.main()
