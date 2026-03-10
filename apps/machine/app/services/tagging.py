from collections import Counter
from typing import Iterable, Sequence, Tuple


def generate_tags(text: str, tag_pool: Sequence[str] | None = None) -> Tuple[list[str], dict[str, float]]:
    """Naive keyword-based tagger; replace with ML model as needed."""
    normalized_words = [w.strip(".,!?;:").lower() for w in text.split() if w.strip()]
    pool = [t.lower() for t in tag_pool] if tag_pool else ["happy", "sad", "calm", "angry"]

    counts = Counter(word for word in normalized_words if word in pool)
    if not counts:
        top = pool[:2]
        scores = {tag: 0.0 for tag in top}
        return top, scores

    top = [tag for tag, _ in counts.most_common(3)]
    total = len(normalized_words) or 1
    scores = {tag: round(freq / total, 4) for tag, freq in counts.items()}
    return top, scores
