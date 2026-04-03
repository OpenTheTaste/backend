package com.ott.domain.short_form.repository;

import com.ott.domain.common.Status;
import com.ott.domain.media.domain.QMedia;
import com.ott.domain.short_form.domain.ShortForm;
import com.ott.domain.media.domain.MediaStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.media_tag.domain.QMediaTag.mediaTag;
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
                .where(
                        media.id.eq(mediaId),
                        media.mediaStatus.eq(MediaStatus.COMPLETED))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<ShortForm> findWithMediaAndUploaderByShortFormId(Long shortFormId) {
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
                .where(
                        shortForm.id.eq(shortFormId),
                        media.mediaStatus.eq(MediaStatus.COMPLETED))
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

    @Override
    public List<ShortForm> findRecommendedShortForms(Map<Long, Integer> tagScores, int limit, long offset) {
        
        // 1. 시청 이력이 없는 신규 유저 방어 로직: 원본 콘텐츠의 북마크 순으로 노출
        if (tagScores == null || tagScores.isEmpty()) {

            // 시리즈 소속이면 시리즈 북마크, 단편이면 단편 북마크를 기준으로 
            NumberExpression<Long> originBookmarkCount = new CaseBuilder()
                    .when(shortForm.series.isNotNull()).then(series.media.bookmarkCount)
                    .otherwise(contents.media.bookmarkCount);


            return queryFactory.selectFrom(shortForm)
                    .leftJoin(shortForm.series, series)
                    .leftJoin(shortForm.contents, contents)
                    .where(shortForm.status.eq(Status.ACTIVE),
                            shortForm.media.mediaStatus.eq(MediaStatus.COMPLETED)) // 활성 상태의 숏폼만
                    // 무한 스와이프를 위해 DB에서 정렬 후 자름
                    .orderBy(originBookmarkCount.desc().nullsLast(), shortForm.createdDate.desc(), shortForm.id.desc())
                    .limit(limit)
                    .offset(offset)
                    .fetch();
        }

        // 2. 가중치 합산 로직: 숏폼의 '본편 미디어(Media)'에 달린 태그와 유저 취향 점수를 매칭
        // 초기값은 0으로 표기.
        NumberExpression<Integer> scoreExpression = Expressions.asNumber(0);

        // tagScores 에 대한 SQL 수학식 생성
        for (Map.Entry<Long, Integer> entry : tagScores.entrySet()) {
            scoreExpression = scoreExpression.add(
                    new CaseBuilder()
                            .when(mediaTag.tag.id.eq(entry.getKey())).then(entry.getValue())
                            .otherwise(0)
            );
        }

        return queryFactory.selectFrom(shortForm)
                .leftJoin(shortForm.series, series)
                .leftJoin(shortForm.contents, contents)
                // 숏폼의 부모(시리즈 or 단편)에 따라 정확한 원본 태그를 조인!
                .leftJoin(mediaTag).on(
                        shortForm.series.isNotNull().and(mediaTag.media.id.eq(series.media.id))
                        .or(shortForm.series.isNull().and(mediaTag.media.id.eq(contents.media.id)))
                )
                .where(shortForm.status.eq(Status.ACTIVE),
                        shortForm.media.mediaStatus.eq(MediaStatus.COMPLETED)) // 활성 상태의 숏폼만
                .groupBy(shortForm.id)
                .orderBy(scoreExpression.sum().desc(), shortForm.createdDate.desc(), shortForm.id.desc())
                .limit(limit)
                .offset(offset)
                .fetch();
    }

    @Override
    public List<ShortForm> findLatestShortForms(int limit, long offset, List<Long> excludeIds) {
        
        // 추천 리스트에 이미 들어간 숏폼 ID 제외 
        BooleanExpression excludeCondition = (excludeIds != null && !excludeIds.isEmpty())
                ? shortForm.media.id.notIn(excludeIds)
                : null;

        return queryFactory.selectFrom(shortForm)
                .where(
                        excludeCondition,
                        shortForm.status.eq(Status.ACTIVE),
                        shortForm.media.mediaStatus.eq(MediaStatus.COMPLETED)// 활성 상태의 숏폼만
                )
                .orderBy(shortForm.createdDate.desc(), shortForm.id.desc()) // 무조건 최신 업로드 순
                .limit(limit)
                .offset(offset)
                .fetch();
    }
}
