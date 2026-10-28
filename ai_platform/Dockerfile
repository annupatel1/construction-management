# Dockerfile for Multimodal Construction Intelligence Platform
# Skill Wallet Capstone Project 1 | Platform ID: 6ac4c98d1dcde6d7857c1642

FROM python:3.11-slim

# Prevent Python from writing .pyc and buffer output
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt httpx

# Copy project files
COPY app.py backend_api.py ai_engine.py planning_engine.py weather_engine.py test_suite.py ./
COPY samples/ ./samples/

# Expose Streamlit and FastAPI ports
EXPOSE 8501 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8501/_stcore/health || exit 1

# Default command launches turnkey Streamlit app
CMD ["streamlit", "run", "app.py", "--server.port=8501", "--server.address=0.0.0.0"]
