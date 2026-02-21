package com.ott.domain.media.repository;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ott.domain.media.domain.QMedia.media;

@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Media> findMediaListByMediaType(Pageable pageable, MediaType mediaType, String searchWord) {
        List<Media> mediaList = queryFactory
                .selectFrom(media)
                .where(
                        media.mediaType.eq(mediaType),
                        titleContains(searchWord)
                )
                .orderBy(media.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(media.count())
                .from(media)
                .where(
                        media.mediaType.eq(mediaType),
                        titleContains(searchWord)
                );

        return PageableExecutionUtils.getPage(mediaList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression titleContains(String searchWord) {
        if (StringUtils.hasText(searchWord))
            return media.title.contains(searchWord);
        return null;
    }
}
