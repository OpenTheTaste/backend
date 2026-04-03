package com.ott.api_user.likes.controller;

import com.ott.api_user.likes.dto.request.LikesRequest;
import com.ott.common.web.exception.ErrorResponse;
import com.ott.common.web.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Likes API", description = "좋아요 API")
public interface LikesAPI {

    @Operation(summary = "좋아요 API", description = "좋아요 상태를 변경합니다.  등록/취소 모두 이 API를 사용합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "좋아요 성공"),
            @ApiResponse(responseCode = "404", description = "미디어 또는 사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<SuccessResponse<Void>> editLikes(
            @Valid @RequestBody LikesRequest request,
            @Parameter(hidden = true) Long memberId
    );
}