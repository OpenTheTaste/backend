package com.ott.api_admin.content.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

import com.ott.domain.common.PublicStatus;

@Schema(description = "콘텐츠 상세 조회 응답")
public record ContentsDetailResponse(

                @Schema(type = "Long", description = "콘텐츠 ID", example = "1") Long contentsId,

                @Schema(type = "String", description = "포스터 URL", example = "https://cdn.example.com/poster.jpg") String posterUrl,

                @Schema(type = "String", description = "썸네일 URL", example = "https://cdn.example.com/thumb.jpg") String thumbnailUrl,

                @Schema(type = "String", description = "콘텐츠 제목", example = "기생충") String title,

                @Schema(type = "String", description = "콘텐츠 설명", example = "봉준호 감독의 블랙코미디 스릴러") String description,

                @Schema(type = "String", description = "출연진", example = "송강호, 이선균") String actors,

                @Schema(type = "String", description = "소속 시리즈 제목 (없으면 null)", example = "비밀의 숲") String seriesTitle,

                @Schema(type = "String", description = "업로더 닉네임", example = "관리자") String uploaderNickname,

                @Schema(type = "Integer", description = "영상 길이(초)", example = "7200") Integer duration,

                @Schema(type = "Integer", description = "영상 크기(KB)", example = "1048576") Integer videoSize,

                @Schema(type = "String", description = "카테고리명", example = "드라마") String categoryName,

                @Schema(type = "List<String>", description = "태그 이름 목록", example = "[\"스릴러\", \"추리\"]") List<String> tagNameList,

                @Schema(type = "String", description = "공개 여부", example = "PUBLIC") PublicStatus publicStatus,

                @Schema(type = "Long", description = "북마크 수", example = "150") Long bookmarkCount,

                @Schema(type = "LocalDate", description = "업로드일", example = "2026-01-15") LocalDate uploadedDate) {
}
