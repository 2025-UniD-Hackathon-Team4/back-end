package com.example.oauthsession.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

public class CaffeineResponse {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddCaffeineResponseDto{
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dateTime;
        String storeName;
        String size;
        Integer caffeineMg;
        String menuName;
    }
}
