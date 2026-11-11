# Multimodal Construction Intelligence AI service
FROM docker.io/library/python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

# Run the service as an unprivileged user.
RUN useradd --create-home --shell /bin/bash appuser

# curl is used by the container health check
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt requirements-api.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY --chown=appuser:appuser app.py backend_api.py ai_engine.py planning_engine.py weather_engine.py test_suite.py ./
COPY --chown=appuser:appuser samples/ ./samples/

USER appuser

EXPOSE 8000 8501

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD curl -f http://127.0.0.1:8000/api/health || exit 1

# Default: the FastAPI service used by the React app.
# Run the Streamlit console with:
#   docker run ... streamlit run app.py --server.port=8501 --server.address=0.0.0.0
CMD ["uvicorn", "backend_api:app", "--host", "0.0.0.0", "--port", "8000"]
