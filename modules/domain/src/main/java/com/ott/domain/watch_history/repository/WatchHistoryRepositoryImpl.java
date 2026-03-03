package com.ott.domain.watch_history.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media_tag.domain.QMediaTag.mediaTag;
import static com.ott.domain.tag.domain.QTag.tag;
import static com.ott.domain.watch_history.domain.QWatchHistory.watchHistory;

@RequiredArgsConstructor
public class WatchHistoryRepositoryImpl implements WatchHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TagViewCountProjection> countByTagAndCategoryIdAndWatchedBetween(Long categoryId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .select(Projections.constructor(TagViewCountProjection.class,
                        tag.name,
                        watchHistory.count()
                ))
                .from(tag)
                .join(mediaTag).on(mediaTag.tag.id.eq(tag.id))
                .join(contents).on(contents.media.id.eq(mediaTag.media.id))
                .join(watchHistory).on(watchHistory.contents.id.eq(contents.id))
                .where(
                        tag.category.id.eq(categoryId),
                        watchHistory.lastWatchedAt.goe(startDate),
                        watchHistory.lastWatchedAt.lt(endDate)
                )
                .groupBy(tag.id, tag.name)
                .fetch();
    }

    @Override
    public Optional<Long> findLatestContentMediaIdByMemberIdAndSeriesId(Long memberId, Long seriesId){
        Long resultMediaId = queryFactory
                .select(contents.media.id)
                .from(watchHistory)
                .join(contents).on(watchHistory.contents.id.eq(contents.id))
                .where(
                        watchHistory.member.id.eq(memberId), 
                        contents.series.id.eq(seriesId)
                )
                .orderBy(watchHistory.lastWatchedAt.desc())
                .fetchFirst();
        return Optional.ofNullable(resultMediaId);
    }
}
