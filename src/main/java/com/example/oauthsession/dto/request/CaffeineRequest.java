package com.example.oauthsession.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

public class CaffeineRequest {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddCaffeineRequestDto{
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSX")
        LocalDateTime dateTime;
         String storeName;
         String size;
         String menuName;
    }



}
