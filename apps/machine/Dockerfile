FROM python:3.12-slim

# 시스템 의존성 설치 (필요한 경우)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 1단계: Pip 업그레이드 및 타임아웃 대폭 상향
RUN pip install --upgrade pip
# 기본 타임아웃을 2000초로 설정하여 웬만한 지연은 견디게 합니다.
ARG PIP_RETRY=20
ARG PIP_TIMEOUT=2000

# 2단계: 가장 무거운 torch를 먼저 설치 (이 부분이 성공하면 캐시됨)
# 특정 버전을 명시하는 것이 빌드 안정성에 좋습니다.
RUN pip install --no-cache-dir --default-timeout=${PIP_TIMEOUT} torch --index-url https://download.pytorch.org/whl/cpu

# 3단계: transformers와 numpy 설치
RUN pip install --no-cache-dir --default-timeout=${PIP_TIMEOUT} transformers numpy

# 4단계: 나머지 일반 라이브러리 설치
COPY apps/machine/requirements.txt .
# requirements.txt에서 이미 설치한 torch, transformers, numpy는 무시됩니다.
RUN pip install --no-cache-dir --default-timeout=${PIP_TIMEOUT} -r requirements.txt

# 5단계: 모델 파일 복사 (용량이 크므로 설치 이후에 진행)
COPY apps/machine/models/tagging /app/models/tagging
COPY apps/machine/models/recommend /app/models/recommend

# 6단계: 소스 코드 복사
COPY apps/machine/ ./machine/
ENV PYTHONPATH=/app/machine

WORKDIR /app/machine
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]