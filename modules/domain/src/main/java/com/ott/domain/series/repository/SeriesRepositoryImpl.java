package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.member.domain.QMember.member;
import static com.ott.domain.series.domain.QSeries.series;

@RequiredArgsConstructor
public class SeriesRepositoryImpl implements SeriesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Series> findWithMediaAndUploaderByMediaId(Long mediaId) {
        Series result = queryFactory
                .selectFrom(series)
                .join(series.media, media).fetchJoin()
                .join(media.uploader, member).fetchJoin()
                .where(media.id.eq(mediaId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
