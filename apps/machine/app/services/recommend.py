import logging
import pickle

from typing import Sequence
from pathlib import Path
from app.config import settings

#MLP 모델을 통해 사용자의 현재 기분 태그에서 환기용 태그 정답 4개를 뽑아냄.

# 가중치 롤북 가져오기
from app.constants.mood_rules import WEIGHT_BIAS

logger = logging.getLogger(__name__)

MODEL = None


if settings.mood_model_path:
    model_file = Path(settings.mood_model_path)
    if model_file.exists():
        try:
            with model_file.open("rb") as f:
                MODEL = pickle.load(f)
            logger.info("분위기 환기용 NLP 모델 로드 완료")
        except Exception as e:
            logger.error(f"모델 로드 실패:{e}")
    else:
        logger.warning(f"환기용 모델 파일을 찾을 수 없습니다:{settings.mood_model_path}")



def infer_targets(input_tags: Sequence[str], limit: int = 4) -> list[str]:
    tag_set = set(input_tags)
    
    all_possible_targets = set(
        target for targets_dict in WEIGHT_BIAS.values() for target in targets_dict.keys()
    )
    bias = {target: 0 for target in all_possible_targets}
    
    for tag in tag_set:
        if tag in WEIGHT_BIAS:
            for target, score in WEIGHT_BIAS[tag].items():
                bias[target] += score

    if MODEL:
        sorted_targets = sorted(bias.keys())
        features = [bias[tag] for tag in sorted_targets]
        probs = MODEL.predict_proba([features])[0]
        ranked = sorted(zip(sorted_targets, probs), key=lambda x: x[1], reverse=True)
        return [tag for tag, _ in ranked[:limit]]
    
    else:
        sorted_tags = sorted(bias.items(), key=lambda x: x[1], reverse=True)
        return [tag for tag, _ in sorted_tags[:limit]]
    
    
