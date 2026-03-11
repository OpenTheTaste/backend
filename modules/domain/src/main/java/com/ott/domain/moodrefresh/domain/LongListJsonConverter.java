package com.ott.domain.moodrefresh.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class LongListJsonConverter implements AttributeConverter<List<Long>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    // 1. Java List -> DB JSON String 저장할 때
    @Override
    public String convertToDatabaseColumn(List<Long> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("List를 JSON으로 변환하는 데 실패했습니다.", e);
        }
    }

    // 2. DB JSON String -> Java List 꺼내올 때
    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            return mapper.readValue(dbData, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON을 List로 변환하는 데 실패했습니다.", e);
        }
    }
}