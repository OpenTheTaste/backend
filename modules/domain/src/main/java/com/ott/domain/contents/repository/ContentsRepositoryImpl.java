package com.ott.domain.contents.repository;

import com.ott.domain.contents.domain.Contents;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.member.domain.QMember.member;
import static com.ott.domain.series.domain.QSeries.series;

@RequiredArgsConstructor
public class ContentsRepositoryImpl implements ContentsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Contents> findWithMediaAndUploaderByMediaId(Long mediaId) {
        Contents result = queryFactory
                .selectFrom(contents)
                .join(contents.media, media).fetchJoin()
                .join(media.uploader, member).fetchJoin()
                .leftJoin(contents.series, series).fetchJoin()
                .leftJoin(series.media).fetchJoin()
                .where(media.id.eq(mediaId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
