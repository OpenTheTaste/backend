package com.ott.api_user.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Schema(description = "시청 이력 생성 DTO")
public class WatchHistoryRequest {

    @Schema(type =  "Long", description = "미디어 ID", example = "101")
    private Long mediaId;
}
