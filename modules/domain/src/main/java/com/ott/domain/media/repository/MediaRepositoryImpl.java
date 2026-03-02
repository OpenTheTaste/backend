package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.media.domain.Media;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import com.querydsl.jpa.JPAExpressions;

import static com.ott.domain.common.MediaType.CONTENTS;
import static com.ott.domain.common.MediaType.SERIES;
import static com.ott.domain.common.PublicStatus.PUBLIC;
import static com.ott.domain.common.Status.ACTIVE;
import static com.ott.domain.contents.domain.QContents.contents;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.media_tag.domain.QMediaTag.mediaTag;

@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<Media> findMediaListByMediaTypeAndSearchWord(Pageable pageable, MediaType mediaType,
                        String searchWord) {
                List<Media> mediaList = queryFactory
                                .selectFrom(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord))
                                .orderBy(media.createdDate.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(media.count())
                                .from(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord));

                return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatus(Pageable pageable, MediaType mediaType,
                        String searchWord, PublicStatus publicStatus) {
                List<Media> mediaList = queryFactory
                                .selectFrom(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord),
                                                publicStatusEq(publicStatus))
                                .orderBy(media.createdDate.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(media.count())
                                .from(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord),
                                                publicStatusEq(publicStatus));

                return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findMediaListByMediaTypeAndSearchWordAndPublicStatusAndUploaderId(Pageable pageable,
                        MediaType mediaType, String searchWord, PublicStatus publicStatus, Long uploaderId) {
                List<Media> mediaList = queryFactory
                                .selectFrom(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord),
                                                publicStatusEq(publicStatus),
                                                uploaderIdEq(uploaderId))
                                .orderBy(media.createdDate.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(media.count())
                                .from(media)
                                .where(
                                                mediaTypeEq(mediaType),
                                                titleContains(searchWord),
                                                publicStatusEq(publicStatus),
                                                uploaderIdEq(uploaderId));

                return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findOriginMediaListBySearchWord(Pageable pageable, String searchWord) {
                BooleanExpression condition = media.mediaType.in(List.of(SERIES, CONTENTS))
                                .and(
                                                JPAExpressions.selectOne()
                                                                .from(contents)
                                                                .where(
                                                                                contents.media.id.eq(media.id),
                                                                                contents.series.isNotNull())
                                                                .notExists());

                List<Media> mediaList = queryFactory
                                .selectFrom(media)
                                .where(
                                                condition,
                                                titleContains(searchWord))
                                .orderBy(media.createdDate.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(media.count())
                                .from(media)
                                .where(condition, titleContains(searchWord));

                return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
        }

        /**
         * 특정 태그에 속하는 추천 콘텐츠 조회
         */
        @Override
        public List<TagContentProjection> findRecommendContentsByTagId(Long tagId, int limit) {
                return queryFactory
                        .select(Projections.constructor(TagContentProjection.class,
                                media.id,
                                media.posterUrl,
                                media.mediaType
                        ))
                        .from(media)
                        .join(mediaTag).on(
                                mediaTag.media.id.eq(media.id),
                                mediaTag.tag.id.eq(tagId),
                                mediaTag.status.eq(ACTIVE)
                        )
                        .leftJoin(contents).on(
                                contents.media.id.eq(media.id),
                                contents.series.isNull()
                        )
                        .where(
                                media.status.eq(ACTIVE),
                                media.publicStatus.eq(PUBLIC),
                                // 시리즈 자체 OR 단편 콘텐츠 (시리즈 에피소드 제외)
                                media.mediaType.eq(SERIES)
                                        .or(media.mediaType.eq(CONTENTS).and(contents.id.isNotNull()))
                        )
                        .orderBy(media.bookmarkCount.desc())  // 북마크 많은 순 정렬
                        .limit(limit)
                        .fetch();
        }

        private BooleanExpression titleContains(String searchWord) {
                if (StringUtils.hasText(searchWord))
                        return media.title.contains(searchWord);
                return null;
        }

        private BooleanExpression mediaTypeEq(MediaType mediaType) {
                if (mediaType != null)
                        return media.mediaType.eq(mediaType);
                return null;
        }

        private BooleanExpression publicStatusEq(PublicStatus publicStatus) {
                if (publicStatus != null)
                        return media.publicStatus.eq(publicStatus);
                return null;
        }

        private BooleanExpression uploaderIdEq(Long uploaderId) {
                if (uploaderId != null)
                        return media.uploader.id.eq(uploaderId);
                return null;
        }
}
