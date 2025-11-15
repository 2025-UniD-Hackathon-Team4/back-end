package com.example.oauthsession.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CaffeineRequest {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddCaffeineRequestDto{
        @Schema(
                description = "섭취 시간 (yyyy-MM-dd'T'HH:mm:ss 형식)",
                example = "2025-11-15T11:23:45"
        )
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
        @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
         LocalDateTime dateTime;

        @Schema(
                description = "가게 이름",
                example = "투썸플레이스"
        )
         String storeName;

         @Schema(
                description = "사이즈",
                example = "regular"
        )
         String size;

        @Schema(
                description = "메뉴 이름",
                example = "카페라떼"
        )
         String menuName;
    }



}
