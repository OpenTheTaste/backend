package com.ott.api_user.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "시청이력 기반 태그 랭킹 응답 DTO")
public class TagRankingResponse {

    @Schema(description = "태그 랭킹 목록 (상위 4개 + 기타 1개, 최대 5개)")
    private List<TagRankItem> rankings;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "태그 랭킹 아이템")
    public static class TagRankItem {

        @Schema(type = "Long", example = "3", description = "태그 ID (기타 항목은 null)")
        private Long tagId;

        @Schema(type = "String", example = "스릴러", description = "태그명 (기타 항목은 '기타')")
        private String tagName;

        @Schema(type = "Long", example = "12", description = "시청 횟수")
        private Long count;

        @Schema(type = "boolean", example = "false", description = "기타 항목 여부")
        private boolean isEtc;

        public static TagRankItem of(Long tagId, String tagName, Long count) {
            return TagRankItem.builder()
                    .tagId(tagId)
                    .tagName(tagName)
                    .count(count)
                    .isEtc(false)
                    .build();
        }

        public static TagRankItem ofEtc(Long totalCount) {
            return TagRankItem.builder()
                    .tagId(null)
                    .tagName("기타")
                    .count(totalCount)
                    .isEtc(true)
                    .build();
        }
    }
}