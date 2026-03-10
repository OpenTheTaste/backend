from typing import List

_DEFAULT_ITEMS = [
    "Take a short walk",
    "Stretch for 5 minutes",
    "Listen to a favorite song",
    "Drink water",
    "Write down one gratitude",
    "Deep breathing x10",
]


def rank_refresh(mood: str, limit: int = 5) -> List[str]:
    # Placeholder rule-based ranking: swap order a bit based on mood length.
    rotation = len(mood) % len(_DEFAULT_ITEMS)
    rotated = _DEFAULT_ITEMS[rotation:] + _DEFAULT_ITEMS[:rotation]
    return rotated[:limit]
