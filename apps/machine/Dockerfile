FROM python:3.12-slim

WORKDIR /app

COPY apps/machine/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY apps/machine/ ./machine/
ENV PYTHONPATH=/app/machine

WORKDIR /app/machine
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
