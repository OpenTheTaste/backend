package com.ott.domain.series.repository;

import com.ott.domain.series.domain.Series;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ott.domain.series.domain.QSeries.series;

@RequiredArgsConstructor
public class SeriesRepositoryImpl implements SeriesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Series> findSeriesList(Pageable pageable, String searchWord) {
        List<Series> seriesList = queryFactory
                .selectFrom(series)
                .where(titleContains(searchWord))
                .orderBy(series.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(series.count())
                .from(series)
                .where(titleContains(searchWord));

        return PageableExecutionUtils.getPage(seriesList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression titleContains(String searchWord) {
        if (StringUtils.hasText(searchWord))
            return series.title.contains(searchWord);
        return null;
    }
}
