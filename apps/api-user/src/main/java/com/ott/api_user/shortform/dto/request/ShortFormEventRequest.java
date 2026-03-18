package com.ott.api_user.shortform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShortFormEventRequest {

    @NotNull(message = "숏폼 ID 는 필수입니다.")
    @Schema(type = "Long" , description = "이벤트가 발생한 숏폼 ID", example = "5")
    private Long mediaId;
}
