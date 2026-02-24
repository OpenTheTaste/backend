package com.ott.api_user.member.dto.response;

import com.ott.domain.member.domain.Member;
import com.ott.domain.preferred_tag.domain.PreferredTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "마이페이지 조회 DTO")
public class MyPageResponse {

    @Schema(type = "Long", example = "1", description = "회원 고유 ID")
    private Long memberId;

    @Schema(type = "String", example = "김마루", description = "닉네임")
    private String nickname;

    @Schema(type = "List", example = "[영화 | 판타지, 드라마 | 로맨스]", description = "선호 태그 목록")
    private List<String> preferredTags;


    public static MyPageResponse from(Member member, List<PreferredTag> preferredTags) {
        List<String> displays = preferredTags.stream()
                .map(pt -> pt.getTag().getCategory().getName() + " | " + pt.getTag().getName())
                .toList();

        return MyPageResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .preferredTags(displays) // 선호태그 없으면 빈 리스트 출력
                .build();
    }

}
