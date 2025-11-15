package com.example.oauthsession.dto.request;

import lombok.*;

import java.time.LocalDateTime;

public class CaffeineRequest {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddCaffeineRequestDto{
         LocalDateTime dateTime;
         String storeName;
         String size;
    }


}
