package com.ott.domain.contents.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.member.domain.Member;
import com.ott.domain.series.domain.Series;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private Member uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "actors", nullable = false)
    private String actors;

    @Column(name = "poster_url", nullable = false, columnDefinition = "TEXT")
    private String posterUrl;

    @Column(name = "thumbnail_url", nullable = false, columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "video_size")
    private Integer videoSize;

    @Column(name = "bookmark_count", nullable = false)
    private Long bookmarkCount;

    @Column(name = "likes_count", nullable = false)
    private Long likesCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "public_status", nullable = false)
    private PublicStatus publicStatus;

    @Column(name = "origin_url", nullable = false, columnDefinition = "TEXT")
    private String originUrl;

    @Column(name = "master_playlist_url", columnDefinition = "TEXT")
    private String masterPlaylistUrl;
}
