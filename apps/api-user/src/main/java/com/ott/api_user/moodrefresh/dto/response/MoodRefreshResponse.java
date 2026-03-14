package com.ott.api_user.moodrefresh.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.encrypt.BytesEncryptor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ott.domain.media.domain.Media;
import com.ott.domain.moodrefresh.domain.MemberMoodRefresh;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoodRefreshResponse {
    private Long refreshId;
    private Byte imageId;
    private String subtitle;
    private List<MoodRecommendMediaDto> recommendedMediaList;

    public static MoodRefreshResponse of(MemberMoodRefresh refresh, List<Media> mediaList) {
        // List 를 dto 로 변환
        List<MoodRecommendMediaDto> mediaDtoList = mediaList.stream()
                .map(MoodRecommendMediaDto::from)
                .collect(Collectors.toList());

        return MoodRefreshResponse.builder()
                .refreshId(refresh.getId())
                .imageId(refresh.getImageId())
                .subtitle(refresh.getSubtitle())
                .recommendedMediaList(mediaDtoList)
                .build();
    }
    
}
