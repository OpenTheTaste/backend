package com.ott.domain.member_radar_preference.domain;

import com.ott.domain.common.BaseEntity;
import com.ott.domain.member.domain.Member;
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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "member_radar_preference")
public class MemberRadarPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "popularity", nullable = false)
    private Integer popularity;

    @Column(name = "immersion", nullable = false)
    private Integer immersion;

    @Column(name = "mania", nullable = false)
    private Integer mania;

    @Column(name = "recency", nullable = false)
    private Integer recency;

    @Column(name = "re_watch", nullable = false)
    private Integer reWatch;

    public static MemberRadarPreference createDefault(Member member) {
        return MemberRadarPreference.builder()
                .member(member)
                .popularity(0)
                .immersion(0)
                .mania(0)
                .recency(0)
                .reWatch(0)
                .build();
    }

    public void updatePreference(Integer popularity, Integer immersion, Integer mania, Integer recency, Integer reWatch) {
        this.popularity = popularity;
        this.immersion = immersion;
        this.mania = mania;
        this.recency = recency;
        this.reWatch = reWatch;
    }
}
