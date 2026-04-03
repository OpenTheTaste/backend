package com.ott.domain.media_metrics.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.media.domain.Media;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "media_metrics")
public class MediaMetrics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false, unique = true)
    private Media media;

    @Column(name = "popularity", nullable = false)
    private BigDecimal popularity;

    @Column(name = "immersion", nullable = false)
    private BigDecimal immersion;

    @Column(name = "mania", nullable = false)
    private BigDecimal mania;

    @Column(name = "recency", nullable = false)
    private BigDecimal recency;

    @Column(name = "re_watch", nullable = false)
    private BigDecimal reWatch;

    @Column(name = "batch_updated_at", nullable = false)
    private LocalDateTime batchUpdatedAt;

    public void updateMetrics(BigDecimal popularity, BigDecimal immersion,
                              BigDecimal mania, BigDecimal recency, BigDecimal reWatch) {
        this.popularity = popularity;
        this.immersion = immersion;
        this.mania = mania;
        this.recency = recency;
        this.reWatch = reWatch;
        this.batchUpdatedAt = LocalDateTime.now();
    }
}
