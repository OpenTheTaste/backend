package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.common.PublicStatus;
import com.ott.domain.common.Status;
import com.ott.domain.media.domain.Media;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import static com.ott.domain.playback.domain.QPlayback.playback;
import static com.ott.domain.contents.domain.QContents.contents;
import java.util.List;
import java.util.Map;

import com.querydsl.jpa.JPAExpressions;

import static com.ott.domain.common.MediaType.CONTENTS;
import static com.ott.domain.common.MediaType.SERIES;
import static com.ott.domain.common.PublicStatus.PUBLIC;
import static com.ott.domain.common.Status.ACTIVE;
import static com.ott.domain.media.domain.QMedia.media;
import static com.ott.domain.bookmark.domain.QBookmark.bookmark;
import static com.ott.domain.media_tag.domain.QMediaTag.mediaTag;




@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        
        // ============================================================
        // 백오피스 관련 쿼라
        // ============================================================

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

        // ============================================================
        // 유저 API 관련 쿼라
        // ============================================================


        // 특정 태그를 가진 영상 조회
        @Override
        public List<Media> findMediasByTagId(Long tagId, Long excludeMediaId, int limit, long offset) {
                return queryFactory.selectFrom(media)
                        .join(mediaTag).on(mediaTag.media.id.eq(media.id))
                        .where(
                                media.status.eq(Status.ACTIVE),
                                media.publicStatus.eq(PublicStatus.PUBLIC),
                                mediaTag.tag.id.eq(tagId),
                                excludeMediaId != null ? media.id.ne(excludeMediaId) : null)
                        .orderBy(media.id.desc())
                        .limit(limit)
                        .offset(offset)
                        .fetch();
        }

        // 특정 태그에 속하는 추천 콘텐츠 조회
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


        /*
         * 플레이리스트 전략패턴 관련 로직
         */

        @Override
        public Page<Media> findTrendingPlaylists(MediaType mediaType, Long excludeMediaId, Pageable pageable) {
                List<Media> content = queryFactory
                                .selectFrom(media)
                                .where(         
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(), // 활성 및 공개 상태 필터링
                                                isDisplayable(), // 공통 노출 조건 사용
                                                excludeId(excludeMediaId) // 현재 미디어 제외 (null이면 무시됨)
                                )
                                .orderBy(media.bookmarkCount.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(media.count())
                                .from(media)
                                .where(         
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(),
                                                isDisplayable(),
                                                excludeId(excludeMediaId));

                // PageableExecutionUtils를 사용하여 첫 페이지 조회 시 불필요한 카운트 쿼리 방지
                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findHistoryPlaylists(Long memberId, MediaType mediaType, Long excludeMediaId, Pageable pageable) {
                List<Media> content = queryFactory
                                .select(media)
                                .from(playback)
                                .join(playback.contents.media, media) // 시청 기록과 미디어 정보 조인
                                .where(
                                                playback.member.id.eq(memberId), // 특정 사용자 필터링
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(), // 활성/공개 상태 확인
                                                isDisplayable(),
                                                excludeId(excludeMediaId) // 현재 재생 중인 영상 제외
                                )
                                .orderBy(playback.modifiedDate.desc()) // 최근 시청 시점 순 정렬
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(playback.count())
                                .from(playback)
                                .join(playback.contents.media, media)
                                .where(
                                                playback.member.id.eq(memberId),
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(),
                                                isDisplayable(),
                                                excludeId(excludeMediaId));

                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findBookmarkedPlaylists(Long memberId, MediaType mediaType, Long excludeMediaId, Pageable pageable) {
                List<Media> content = queryFactory
                                .select(media)
                                .from(bookmark)
                                .join(bookmark.media, media)
                                .where(
                                                bookmark.member.id.eq(memberId),
                                                mediaTypeEq(mediaType),
                                                bookmark.status.eq(Status.ACTIVE),
                                                isActiveAndPublic(),
                                                isDisplayable(),
                                                excludeId(excludeMediaId))
                                .orderBy(bookmark.createdDate.desc()) // 최근 북마크한 순서
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(bookmark.count())
                                .from(bookmark)
                                .join(bookmark.media, media)
                                .where(
                                                bookmark.member.id.eq(memberId),
                                                mediaTypeEq(mediaType),
                                                bookmark.status.eq(Status.ACTIVE),
                                                isActiveAndPublic(),
                                                isDisplayable(),
                                                excludeId(excludeMediaId));

                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        @Override
        public Page<Media> findPlaylistsByTag(Long tagId, MediaType mediaType , Long excludeMediaId, Pageable pageable) {
                List<Media> content = queryFactory
                                .select(media)
                                .from(mediaTag)
                                .join(mediaTag.media, media)
                                .where(
                                                mediaTag.tag.id.eq(tagId), // 요청된 태그 ID 필터링
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(), // 활성/공개 상태 확인
                                                isDisplayable(),
                                                excludeId(excludeMediaId) // 현재 재생 중인 영상 제외
                                )
                                .orderBy(media.createdDate.desc()) // 최신 등록 순 정렬
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(mediaTag.count())
                                .from(mediaTag)
                                .join(mediaTag.media, media)
                                .where(
                                                mediaTag.tag.id.eq(tagId),
                                                mediaTypeEq(mediaType),
                                                isActiveAndPublic(),
                                                isDisplayable(),
                                                excludeId(excludeMediaId));

                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        // 특정 태그를 가진 영상 조회
        @Override
        public List<Media> findMediasByTagId( Long tagId, MediaType mediaType, Long excludeMediaId, int limit, long offset) {
                return queryFactory.selectFrom(media)
                                .join(mediaTag).on(mediaTag.media.id.eq(media.id))
                                .where(
                                                mediaTag.tag.id.eq(tagId),
                                                isActiveAndPublic(),
                                                mediaTypeEq(mediaType),
                                                isDisplayable(),
                                                excludeMediaId != null ? media.id.ne(excludeMediaId) : null)
                                .orderBy(media.id.desc())
                                .limit(limit)
                                .offset(offset)
                                .fetch();
        }

        // PlaylistPrefereceService 에서 받아온 tagScores 로 추천 종합 쿼리
        @Override
        public List<Media> findRecommendedMedias(Map<Long, Integer> tagScores, MediaType mediaType, Long excludeMediaId, int limit, long offset) {
                // 시청이력도 없고, 선호태그 조차 고르지 않은 백지 상태의 유저
                // 이때는 가장 최근 신작 노출
                if (tagScores.isEmpty()) {
                        return queryFactory.selectFrom(media)
                                        .where(
                                                        isActiveAndPublic(),
                                                        mediaTypeEq(mediaType),
                                                        isDisplayable(),
                                                        excludeId(excludeMediaId))
                                        .orderBy(media.id.desc())
                                        .limit(limit)
                                        .offset(offset)
                                        .fetch();
                }

                // 개인화 추천을 위한 점수 계산기
                NumberExpression<Integer> scoreExpression = new CaseBuilder()
                                .when(mediaTag.tag.id.isNotNull()).then(0).otherwise(0);

                for (Map.Entry<Long, Integer> entry : tagScores.entrySet()) {
                        scoreExpression = scoreExpression.add(
                                        new CaseBuilder()
                                                        .when(mediaTag.tag.id.eq(entry.getKey())).then(entry.getValue())
                                                        .otherwise(0));
                }

                return queryFactory.selectFrom(media)
                                .join(mediaTag).on(mediaTag.media.id.eq(media.id))
                                .where(
                                                isActiveAndPublic(),
                                                mediaTypeEq(mediaType),
                                                isDisplayable(),
                                                excludeId(excludeMediaId))
                                .groupBy(media.id)
                                .orderBy(scoreExpression.sum().desc(), media.id.desc())
                                .limit(limit)
                                .offset(offset)
                                .fetch();
        }

        // 유저용 통합 검색 (시리즈/단편 검색)
        @Override
        public Page<Media> findUserSearchMediaList(Pageable pageable, String searchWord) {
                List<Media> mediaList = queryFactory
                        .selectFrom(media)
                        .where(
                                isActiveAndPublic(),       // 일반 유저용 핵심 방어 코드 (ACTIVE + PUBLIC)
                                isDisplayable(),           // 숏폼 및 에피소드 제외 로직 재활용
                                titleContains(searchWord)  // 검색어 동적 필터링
                        )
                        .orderBy(media.createdDate.desc(), media.id.desc()) // 최신순 정렬
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

                JPAQuery<Long> countQuery = queryFactory
                        .select(media.count())
                        .from(media)
                        .where(
                                isActiveAndPublic(),
                                isDisplayable(),
                                titleContains(searchWord)
                        );

                return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
        }

        // --- 동적 쿼리 헬퍼 메서드 ---


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

        private BooleanExpression isActiveAndPublic() {
                // Status.ACTIVE와 PublicStatus.PUBLIC 조건을 결합
                return media.status.eq(Status.ACTIVE)
                                .and(media.publicStatus.eq(PublicStatus.PUBLIC));
        }

        private BooleanExpression excludeId(Long excludeMediaId) {
                // 전달된 ID가 있을 때만 '해당 ID 제외' 조건을 추가, 없으면 null 반환하여 무시
                return excludeMediaId != null ? media.id.ne(excludeMediaId) : null;
        }

        private BooleanExpression isDisplayable() {
                return media.mediaType.eq(MediaType.SERIES)
                        .or(media.mediaType.eq(MediaType.CONTENTS)
                                .and(JPAExpressions.selectOne()
                                        .from(contents)
                                        .where(contents.media.id.eq(media.id)
                                                .and(contents.series.isNotNull()))
                                        .notExists()));
       }
}
