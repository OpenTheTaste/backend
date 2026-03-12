import torch
import numpy as np
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import logging

from app.errors import AppException, ErrorCode

logger = logging.getLogger(__name__)

class MoodTagger:
    def __init__(self, model_path: str = "models/klue_saved_model"):
        self.model_path = model_path
        # GPU가 있으면 쓰고, 없으면 CPU 사용
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.tokenizer = None
        self.model = None
        self._load_model()

    def _load_model(self):
        try:
            logger.info(f"ML 모델 로딩 중... 경로: {self.model_path} (Device: {self.device})")
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_path)
            self.model = AutoModelForSequenceClassification.from_pretrained(self.model_path)
            self.model.to(self.device)
            self.model.eval()  # 평가 모드로 전환
            logger.info("ML 모델 로딩 완료!")
        except Exception as e:
            logger.error(f"모델 로딩 실패. 경로에 모델 파일이 있는지 확인하세요: {e}")

    def predict(self, text: str, top_k: int = 3) -> list[str]:
        # 모델이 정상적으로 안 불러와졌을 때의 방어 로직
        if not self.model or not self.tokenizer:
            logger.warning("모델이 로드되지 않아 예외를 발생시킵니다.")
            raise AppException(ErrorCode.INTERNAL_ERROR, "태깅 모델이 로드되지 않았습니다.")

        # 토크나이징
        inputs = self.tokenizer(
            text,
            return_tensors="pt",
            truncation=True,
            max_length=256,
            padding="max_length"
        )
        inputs = {k: v.to(self.device) for k, v in inputs.items()}

        # 예측 수행
        with torch.no_grad():
            outputs = self.model(**inputs)

        logits = outputs.logits
        probs = torch.sigmoid(logits).squeeze(0).cpu().numpy()

        # 확률이 가장 높은 순서대로 정렬하여 상위 top_k개 인덱스 추출
        top_indices = np.argsort(probs)[::-1][:top_k]

        results = []
        for idx in top_indices:
            tag_name = self.model.config.id2label[idx]

            # 돌려보면서 threshold 지점 픽스 예정
            # probability = probs[idx]
            # if probability < 0.3:
            #     continue

            results.append(tag_name)

        return results

# 서버 기동 시 인스턴스를 하나만 생성해 둠
mood_tagger = MoodTagger()
