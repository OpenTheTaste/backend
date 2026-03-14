package com.ott.api_admin.click_event.mapper;

import com.ott.api_admin.click_event.dto.response.ShortFormConversionResponse;
import org.springframework.stereotype.Component;

@Component
public class BackOfficeClickEventMapper {

    public ShortFormConversionResponse toShortFormConversionResponse(double thisMonthRate, double lastMonthRate) {
        return new ShortFormConversionResponse(
                thisMonthRate,
                thisMonthRate - lastMonthRate
        );
    }
}
