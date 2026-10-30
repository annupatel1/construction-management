#!/bin/bash
# Startup script for Multimodal Construction Intelligence Platform

echo "=========================================================="
echo "🏗️  CivicPulse - Multimodal Construction Intelligence Platform"
echo "    Capstone Project 1 | Platform ID: 6ac4c98d1dcde6d7857c1642"
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Activate Python virtual environment
if [ -d "venv" ]; then
    source venv/bin/activate
    echo "✅ Python virtual environment activated."
else
    echo "⚠️ venv not found. Using system python."
fi

# Run test suite
echo "🧪 Running unit & contract tests..."
python3 test_suite.py
if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Aborting startup."
    exit 1
fi
echo "✅ All tests passed successfully!"

# Ask user what service to launch
echo ""
echo "Select deployment mode:"
echo "1) Launch Turnkey Streamlit Platform (Port 8501) [Recommended for 1-Click Showcase]"
echo "2) Launch FastAPI REST Backend (Port 8000)"
echo "3) Launch Full Stack (FastAPI Backend + React Frontend)"
echo "4) Launch Both Streamlit App and FastAPI Backend concurrently"
echo ""

MODE=${1:-1}

case $MODE in
    1)
        echo "🚀 Launching Turnkey Streamlit Application..."
        streamlit run app.py --server.port=8501 --server.address=0.0.0.0
        ;;
    2)
        echo "🚀 Launching FastAPI REST Backend..."
        uvicorn backend_api:app --host 0.0.0.0 --port=8000 --reload
        ;;
    3)
        echo "🚀 Launching FastAPI Backend on :8000..."
        uvicorn backend_api:app --host 0.0.0.0 --port=8000 &
        FASTAPI_PID=$!
        echo "🚀 Launching React Frontend on Vite..."
        cd buildtrack/frontend && npm run dev
        kill $FASTAPI_PID
        ;;
    4)
        echo "🚀 Launching FastAPI Backend on :8000 & Streamlit on :8501..."
        uvicorn backend_api:app --host 0.0.0.0 --port=8000 &
        streamlit run app.py --server.port=8501 --server.address=0.0.0.0
        ;;
    *)
        echo "Invalid mode. Defaulting to Streamlit."
        streamlit run app.py --server.port=8501 --server.address=0.0.0.0
        ;;
esac
