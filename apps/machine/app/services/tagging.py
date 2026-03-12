from collections import Counter
from typing import Sequence, Tuple


def generate_tags(text: str, tag_pool: Sequence[str] | None = None) -> Tuple[list[str], dict[str, float]]:
    """Naive keyword-based tagger; replace with ML model as needed."""
    normalized_words = [w.strip(".,!?;:").lower() for w in text.split() if w.strip()]
    pool = tag_pool or ["happy", "sad", "calm", "angry"]
    pool_map = {tag.lower(): tag for tag in pool}

    counts = Counter(word for word in normalized_words if word in pool_map)
    if not counts:
        fallback = pool[:2]
        scores = {tag: 0.0 for tag in fallback}
        return fallback, scores

    top = [pool_map[tag] for tag, _ in counts.most_common(3)]
    total = len(normalized_words) or 1
    scores = {pool_map[tag]: round(freq / total, 4) for tag, freq in counts.items()}
    return top, scores
