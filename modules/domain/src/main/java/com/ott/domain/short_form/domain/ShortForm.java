package com.ott.domain.short_form.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.contents.domain.Contents;
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

import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "short_form")
public class ShortForm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false, unique = true)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contents_id")
    private Contents contents;

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

    public void updateMetadata(Series series, Contents contents) {
        this.series = series;
        this.contents = contents;
    }

    public Optional<Media> findOriginMedia() {
        if (series != null) return Optional.of(series.getMedia());
        if (contents != null) return Optional.of(contents.getMedia());
        return Optional.empty();
    }
}
