package com.ott.api_user.moodrefresh.dto.response;

import com.ott.domain.common.MediaType;
import com.ott.domain.media.domain.Media;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access =  AccessLevel.PRIVATE)
public class MoodRecommendMediaDto {
        // 분석 결과에 해당하는 미디어 컨텐츠 제공
        private Long mediaId;
        private String title;
        private String posterUrl;
        private MediaType mediaType;

        public static MoodRecommendMediaDto from(Media media) {
            return MoodRecommendMediaDto.builder()
                    .mediaId(media.getId())
                    .title(media.getTitle())
                    .posterUrl(media.getPosterUrl())
                    .mediaType(media.getMediaType())
                    .build();
        }
}
