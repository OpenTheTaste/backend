package com.ott.domain.moodrefresh.domain;

import java.util.List;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.member.domain.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_mood_refresh")
public class MemberMoodRefresh extends BaseEntity { // ✨ 핵심: BaseEntity 상속!

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "image_id", nullable = false)
    private Byte imageId;

    @Column(name = "subtitle", columnDefinition = "TEXT") // 추천 메시지'
    private String subtitle;

    // JSON <-> List 변환 컨버터
    @Convert(converter = LongListJsonConverter.class)
    @Column(name = "recommended_media_ids", columnDefinition = "json")
    private List<Long> recommendedMediaIds;

    @Column(name = "is_exposed", nullable = false)
    private boolean isExposed = false;

    @Builder
    public MemberMoodRefresh(Member member, Byte imageId, String subtitle, List<Long> recommendedMediaIds) {
        this.member = member;
        this.imageId = imageId;
        this.subtitle = subtitle;
        this.recommendedMediaIds = recommendedMediaIds;
        this.isExposed = false;
    }
    
    // 이 결과가 홈에 노출 됐을 때
    public void markAsExposed() {
        this.isExposed = true;
    }
}