package com.ott.api_user.member.controller;

import com.ott.api_user.member.dto.request.SetPreferredTagRequest;
import com.ott.api_user.member.dto.request.UpdateMemberRequest;
import com.ott.api_user.member.dto.response.*;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.PageResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/member")
@Tag(name = "Member", description = "마이페이지 API")
@SecurityRequirement(name = "BearerAuth") // 인증인가 확인
public interface MemberApi {

    // -------------------------------------------------------
    // 마이페이지 조회
    // -------------------------------------------------------
    @Operation(summary = "마이페이지 조회", description = "로그인한 회원의 닉네임과 선호 태그 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(@AuthenticationPrincipal Long memberId);


    // -------------------------------------------------------
    // 내 정보 수정
    // -------------------------------------------------------
    @Operation(summary = "내 정보 수정", description = "닉네임, 선호 태그를 수정합니다. 각 필드는 선택적으로 변경 가능하며 null이면 변경되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "수정 성공 - 변경된 마이페이지 정보 반환",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원 또는 태그를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<SuccessResponse<MyPageResponse>> updateMyInfo(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody UpdateMemberRequest request
    );


    // -------------------------------------------------------
    // 온보딩 선호 태그 저장
    // -------------------------------------------------------
    @Operation(summary = "온보딩에서 선호 태그 저장", description = "온보딩 화면에서 처음 선호 태그를 수집합니다. 유저마다 1회만 실행"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "선호 태그 저장 성공"
            ),
            @ApiResponse(
                    responseCode = "400", description = "잘못된 요청 (빈 태그 목록, 중복 태그 ID 등)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원 또는 태그를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/me/tags")
    ResponseEntity<SuccessResponse<Void>> setPreferredTags(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody SetPreferredTagRequest request
    );


    @Operation(summary = "온보딩 건너뛰기", description = "온보딩을 건너뛸 경우 onboardingCompleted를 true로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "건너뛰기 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/me/onboarding/skip")
    ResponseEntity<Void> skipOnboarding(
            @AuthenticationPrincipal Long memberId);



    // -------------------------------------------------------
    // 시청이력 기반 태그 랭킹 조회
    // -------------------------------------------------------
    @Operation(summary = "시청이력 기반 태그 랭킹 조회", description = "최근 1달간 시청이력을 기반으로 상위 4개 태그 + 기타 항목을 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagRankingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/me/tag/ranking")
    ResponseEntity<SuccessResponse<TagRankingResponse>> getTagRanking(
            @AuthenticationPrincipal Long memberId);


    // -------------------------------------------------------
    // 태그 월별 시청 count 비교
    // -------------------------------------------------------
    @Operation(summary = "태그 월별 시청 count 비교", description = "특정 태그의 이번 달 vs 저번 달 시청 횟수를 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagMonthlyCompareResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원 또는 태그를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/me/tag/ranking/{tagId}")
    ResponseEntity<SuccessResponse<TagMonthlyCompareResponse>> getTagMonthlyCompare(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    );


    // -------------------------------------------------------
    // 태그별 추천 콘텐츠 목록 조회
    // -------------------------------------------------------
    @Operation(summary = "태그별 추천 콘텐츠 목록 조회", description = "해당 태그에 속하는 콘텐츠를 최대 20개 반환"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TagContentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", description = "회원 또는 태그를 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/me/taglist/{tagId}")
    ResponseEntity<SuccessResponse<List<TagContentResponse>>> getRecommendContentsByTag(
            @AuthenticationPrincipal Long memberId,
            @Positive @PathVariable Long tagId
    );


    // -------------------------------------------------------
    // 전체 시청이력 플레이리스트 페이징 조회
    // -------------------------------------------------------
    @Operation(summary = "시청이력 플레이리스트 조회", description = "전체 시청이력을 최신순으로 10개씩 페이징 조회합니다. 이어보기 시점 포함.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(
                    responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me/history/playlist")
    ResponseEntity<SuccessResponse<PageResponse<RecentWatchResponse>>> getWatchHistoryPlaylist(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer page
    );

    // ============================================================
    // 회원 탈퇴
    // ============================================================
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 처리합니다. 카카오 연결 끊기 및 모든 데이터 Soft Delete.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/me")
    ResponseEntity<Void> withdraw(
            @Parameter HttpServletResponse response,
            @AuthenticationPrincipal Long memberId);

}