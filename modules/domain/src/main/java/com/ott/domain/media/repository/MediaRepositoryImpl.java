package com.ott.domain.media.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepositoryCustom {

    private final JPAQueryFactory queryFactory;
}
