package com.ott.api_user.likes.controller;

import com.ott.api_user.likes.dto.request.LikesRequest;
import com.ott.api_user.likes.service.LikesService;
import com.ott.common.web.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikesController implements LikesAPI {

    private final LikesService likesService;

    @Override
    public ResponseEntity<SuccessResponse<Void>> editLikes(
            @Valid @RequestBody LikesRequest request,
            @AuthenticationPrincipal Long memberId) {

        likesService.editLikes(memberId, request.getMediaId());
        return ResponseEntity.ok(SuccessResponse.of(null));
    }
}
