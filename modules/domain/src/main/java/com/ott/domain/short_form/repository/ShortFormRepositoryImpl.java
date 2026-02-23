package com.ott.domain.short_form.repository;

import com.ott.domain.media.domain.QMedia;
import com.ott.domain.short_form.domain.ShortForm;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.member.domain.QMember.member;
import static com.ott.domain.series.domain.QSeries.series;
import static com.ott.domain.short_form.domain.QShortForm.shortForm;

@RequiredArgsConstructor
public class ShortFormRepositoryImpl implements ShortFormRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ShortForm> findWithMediaAndUploaderByMediaId(Long mediaId) {
        QMedia contentsMedia = new QMedia("contentsMedia");
        QMedia seriesMedia = new QMedia("seriesMedia");

        ShortForm result = queryFactory
                .selectFrom(shortForm)
                .join(shortForm.media, media).fetchJoin()
                .join(media.uploader, member).fetchJoin()
                .leftJoin(shortForm.contents, contents).fetchJoin()
                .leftJoin(contents.media, contentsMedia).fetchJoin()
                .leftJoin(shortForm.series, series).fetchJoin()
                .leftJoin(series.media, seriesMedia).fetchJoin()
                .where(media.id.eq(mediaId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<ShortForm> findAllByMediaIdIn(List<Long> mediaIdList) {
        return queryFactory
                .selectFrom(shortForm)
                .where(shortForm.media.id.in(mediaIdList))
                .fetch();
    }
}
