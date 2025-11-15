package com.example.oauthsession.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CaffeineResponse {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddCaffeineResponseDto{
        @Schema(
                description = "섭취 시간 (yyyy-MM-dd'T'HH:mm:ss 형식)",
                example = "2025-11-15T11:23:45"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dateTime;
        String storeName;
        String size;
        Integer caffeineMg;
        String menuName;
    }
}
