package com.ott.domain.contents.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.media.domain.Media;
import com.ott.domain.series.domain.Series;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "contents")
public class Contents extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false, unique = true)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column(name = "actors", nullable = false)
    private String actors;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "video_size")
    private Integer videoSize;

    @Column(name = "origin_url", nullable = false, columnDefinition = "TEXT")
    private String originUrl;

    @Column(name = "master_playlist_url", columnDefinition = "TEXT")
    private String masterPlaylistUrl;

    public void updateStorageKeys(String originUrl, String masterPlaylistUrl) {
        this.originUrl = originUrl;
        this.masterPlaylistUrl = masterPlaylistUrl;
    }

    public void updateMetadata(Series series, String actors, Integer duration, Integer videoSize) {
        this.series = series;
        this.actors = actors;
        this.duration = duration;
        this.videoSize = videoSize;
    }
}
