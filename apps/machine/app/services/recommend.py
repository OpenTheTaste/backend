from typing import Sequence

_DEFAULT_REFRESH_TAGS = [
    "가벼운_웃음",
    "팝콘무비",
    "도파민_폭발",
    "힐링",
    "잔잔한_위로",
    "사이다_전개",
]


def predict_target_tags(negative_tags: Sequence[str], limit: int = 3) -> list[str]:
    if not negative_tags:
        return _DEFAULT_REFRESH_TAGS[:limit]

    rotation = sum(len(tag) for tag in negative_tags) % len(_DEFAULT_REFRESH_TAGS)
    rotated = _DEFAULT_REFRESH_TAGS[rotation:] + _DEFAULT_REFRESH_TAGS[:rotation]
    return rotated[:limit]
