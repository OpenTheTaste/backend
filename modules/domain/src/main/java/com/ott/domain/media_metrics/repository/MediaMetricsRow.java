package com.ott.domain.media_metrics.repository;

import java.math.BigDecimal;

public record MediaMetricsRow(
        Long mediaId,
        BigDecimal popularity,
        BigDecimal immersion,
        BigDecimal mania,
        BigDecimal recency,
        BigDecimal reWatch
) {}
