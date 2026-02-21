package com.ott.domain.media_tag.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediaTagRepositoryImpl implements MediaTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;
}
