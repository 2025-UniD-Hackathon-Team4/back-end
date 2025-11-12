package com.example.oauthsession.service;

import com.example.oauthsession.dto.UpstageRequestDto;
import com.example.oauthsession.dto.UpstageResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UpstageService {

    private final WebClient upstageWebClient;
    private final String apiPath; // "/v1/chat/completions"

    public UpstageService(WebClient upstageWebClient, @Value("${upstage.api.path}") String apiPath) {
        this.upstageWebClient = upstageWebClient;
        this.apiPath = apiPath;
    }

    /**
     * 사용자의 질문(프롬프트)을 받아 Upstage API를 호출하고, 답변 텍스트를 반환합니다.
     *
     * @param modelName 사용할 모델 (예: "solar-pro2")
     * @param userPrompt 사용자 질문 텍스트
     * @param session HttpSession
     * @return Mono<String> Upstage API의 최종 답변
     */
    public Mono<String> getChatResponseText(String modelName, String userPrompt, HttpSession session) {

        // 1. 세션에서 현재까지의 이력 (previousHistory) 가져오기
        List<UpstageRequestDto.Message> currentHistory =
                (List<UpstageRequestDto.Message>) session.getAttribute("chatHistory");
        if (currentHistory == null) {
            currentHistory = new ArrayList<>();
        }

        // 2. API 요청에 사용할 메시지 리스트 생성 (기존 이력 + 현재 사용자 질문)
        List<UpstageRequestDto.Message> messagesForRequest = new ArrayList<>(currentHistory);
        UpstageRequestDto.Message userMessage = new UpstageRequestDto.Message("user", userPrompt);
        messagesForRequest.add(userMessage);

        // 3. 요청 DTO 생성 (messagesForRequest 사용)
        UpstageRequestDto requestDto = new UpstageRequestDto(
                modelName,
                messagesForRequest, // 이전 대화 기록이 모두 포함됨
                false, // stream=false
                null
        );

        System.out.println("--- Upstage API 호출 시작: " + modelName + " ---");

        return this.upstageWebClient.post()
                .uri(apiPath)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(UpstageResponseDto.class)
                .map(response -> {
                    String answer = response.getChoices() != null && !response.getChoices().isEmpty()
                            ? response.getChoices().get(0).getMessage().getContent()
                            : "API로부터 유효한 응답을 받지 못했습니다.";

                    // 4. 세션에 저장할 최종 기록 생성 (요청 메시지 + AI 응답)
                    List<UpstageRequestDto.Message> finalHistory = new ArrayList<>(messagesForRequest);
                    finalHistory.add(new UpstageRequestDto.Message("assistant", answer));

                    // 5. 최종 기록을 세션에 다시 저장 (다음 요청에서 사용될 이력)
                    session.setAttribute("chatHistory", finalHistory);

                    return answer;
                })
                .doOnError(error -> System.err.println("API 호출 실패: " + error.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Upstage API 호출 및 처리 실패", e)));
    }
}