package com.ott.domain.contents.repository;

import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.contents.domain.Contents;
import com.ott.domain.media.domain.QMedia;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.member.domain.QMember.member;
import static com.ott.domain.series.domain.QSeries.series;
import static com.ott.domain.watch_history.domain.QWatchHistory.watchHistory;

@RequiredArgsConstructor
public class ContentsRepositoryImpl implements ContentsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Contents> findAllByMediaIdIn(List<Long> mediaIdList) {
        return queryFactory
                .selectFrom(contents)
                .where(contents.media.id.in(mediaIdList))
                .fetch();
    }

    @Override
    public Optional<Contents> findWithMediaAndUploaderByMediaId(Long mediaId) {
        QMedia seriesMedia = new QMedia("seriesMedia");
        Contents result = queryFactory
                .selectFrom(contents)
                .join(contents.media, media).fetchJoin()
                .join(media.uploader, member).fetchJoin()
                .leftJoin(contents.series, series).fetchJoin()
                .leftJoin(series.media, seriesMedia).fetchJoin()
                .where(media.id.eq(mediaId))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
