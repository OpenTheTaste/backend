package com.ott.domain.playback.repository;

import com.ott.domain.contents.domain.QContents;
import com.ott.domain.media.domain.QMedia;
import com.ott.domain.playback.domain.Playback;
import com.ott.domain.playback.domain.QPlayback;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.ott.domain.common.Status.ACTIVE;
import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.playback.domain.QPlayback.playback;
import static com.ott.domain.series.domain.QSeries.series;

@RequiredArgsConstructor
public class PlaybackRepositoryImpl implements PlaybackRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Playback> findLatestByMemberAndSeriesMediaIds(Long memberId, List<Long> seriesMediaIdList) {
        QMedia seriesMedia = new QMedia("seriesMedia");

        QPlayback subPlayback = new QPlayback("subPlayback");
        QContents subContents = new QContents("subContents");

        return queryFactory
                .selectFrom(playback)
                .join(playback.contents, contents).fetchJoin()
                .join(contents.series, series).fetchJoin()
                .join(series.media, seriesMedia).fetchJoin()
                .where(
                        playback.member.id.eq(memberId),
                        playback.status.eq(ACTIVE),
                        seriesMedia.id.in(seriesMediaIdList),
                        playback.id.eq(
                                JPAExpressions
                                        .select(subPlayback.id.max())
                                        .from(subPlayback)
                                        .join(subPlayback.contents, subContents)
                                        .where(
                                                subPlayback.member.id.eq(memberId),
                                                subPlayback.status.eq(ACTIVE),
                                                subContents.series.id.eq(series.id)
                                        )
                        )
                )
                .fetch();
    }
}
