package com.example.oauthsession.service;

import com.example.oauthsession.dto.request.DaySummariesRequest;
import com.example.oauthsession.dto.response.DaySummaryResponse;
import com.example.oauthsession.entity.CaffeineIntakes;
import com.example.oauthsession.entity.DaySummaries;
import com.example.oauthsession.entity.User;
import com.example.oauthsession.repository.CaffeineIntakesRepository;
import com.example.oauthsession.repository.DaySummariesRepository;
import com.example.oauthsession.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ConditionService {

    private final UserRepository userRepository;
    private final CaffeineIntakesRepository caffeineIntakesRepository;
    private final DaySummariesRepository daySummariesRepository;
    private final UpstageService upstageService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public Mono<DaySummaryResponse> createDailySummary(DaySummariesRequest request, HttpSession session) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.userId()));

        // 오늘 날짜 = 기상 날짜 기준
        LocalDate today = request.sleepEndAt().atZone(KST).toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        // 전날 00:00 ~ 오늘 00:00 사이 카페인 기록
        LocalDateTime yStart = yesterday.atStartOfDay();
        LocalDateTime yEnd = today.atStartOfDay();

        List<CaffeineIntakes> intakes = caffeineIntakesRepository
                .findByUserAndDateTimeBetween(user, yStart, yEnd);

        int totalCaffeine = intakes.stream()
                .map(i -> i.getCaffeineMg() == null ? 0 : i.getCaffeineMg())
                .mapToInt(Integer::intValue)
                .sum();

        int intakeCount = intakes.size();

        LocalDateTime lastIntakeTime = intakes.stream()
                .map(CaffeineIntakes::getDateTime)
                .max(Comparator.naturalOrder())
                .orElse(null);

        long sleepMinutes = Duration.between(request.sleepStartAt(), request.sleepEndAt()).toMinutes();

        // LLM 프롬프트 구성
        String prompt = buildPrompt(
                yesterday,
                totalCaffeine,
                intakeCount,
                lastIntakeTime,
                request.sleepStartAt(),
                request.sleepEndAt(),
                sleepMinutes,
                request.freshness()
        );

        String model = "solar-pro2";

        return upstageService.getChatResponseText(model, prompt, session)
                .map(responseText -> {
                    // LLM 응답에서 컨디션 온도 정수 추출
                    int conditionScore = extractConditionScore(responseText);
                    String conditionSummary = extractConditionSummary(responseText);

                    // DaySummaries 저장 (기존 있으면 업데이트, 없으면 새로 생성)
                    DaySummaries summary = daySummariesRepository
                            .findByUserAndDate(user, today)
                            .orElseGet(DaySummaries::new);

                    summary.setUser(user);
                    summary.setDate(today);
                    summary.setSleepStartAt(request.sleepStartAt());
                    summary.setSleepEndAt(request.sleepEndAt());
                    summary.setFreshness(request.freshness());
                    summary.setPrevDayCaffeine(totalCaffeine);
                    summary.setPrevDayIntakeCount(intakeCount);
                    summary.setConditionScore(conditionScore);
                    summary.setConditionSummary(conditionSummary);

                    DaySummaries saved = daySummariesRepository.save(summary);

                    return new DaySummaryResponse(
                            saved.getId(),
                            conditionScore,
                            conditionSummary
                    );
                });
    }

    private String buildPrompt(
            LocalDate yesterday,
            int totalCaffeine,
            int intakeCount,
            LocalDateTime lastIntakeTime,
            LocalDateTime sleepStart,
            LocalDateTime sleepEnd,
            long sleepMinutes,
            int freshness
    ) {
        String lastTimeStr = (lastIntakeTime == null) ? "섭취 없음" : lastIntakeTime.toString();

        return """
                당신은 사용자의 하루 컨디션을 평가하는 코치입니다.
                아래 정보를 바탕으로 오늘 아침의 컨디션을 0~100 사이의 정수 점수로 평가하세요.

                출력 형식을 반드시 아래처럼만 지켜서 출력하세요.
                다른 텍스트는 추가하지 마세요.

                컨디션온도: <0에서 100 사이의 정수>
                설명: <2~4문장으로 한국어 설명>

                예시:
                컨디션온도: 73
                설명: 어제 카페인 섭취가 적당했고, 수면 시간이 충분해서 전반적으로 양호한 컨디션입니다. 다만 수면 시간이 평소보다 조금 짧아 오후에 가벼운 피로감을 느낄 수 있습니다.

                입력 데이터:
                - 전날 날짜: %s
                - 전날 총 카페인 섭취량(mg): %d
                - 전날 카페인 섭취 횟수: %d
                - 전날 마지막 카페인 섭취 시각: %s
                - 수면 시작 시각: %s
                - 수면 종료 시각: %s
                - 총 수면 시간(분): %d
                - 기상 직후 상쾌함(1~5): %d
                """.formatted(
                yesterday,
                totalCaffeine,
                intakeCount,
                lastTimeStr,
                sleepStart,
                sleepEnd,
                sleepMinutes,
                freshness
        );
    }

    // "컨디션온도: 73" 에서 73만 뽑기
    private int extractConditionScore(String responseText) {
        Pattern pattern = Pattern.compile("컨디션온도\\s*[:：]\\s*(\\d+)");
        Matcher matcher = pattern.matcher(responseText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        // 파싱 실패하면 기본값 50 정도로
        return 50;
    }

    // "설명: ..." 이후 전체를 요약으로 사용
    private String extractConditionSummary(String responseText) {
        int idx = responseText.indexOf("설명:");
        if (idx >= 0) {
            return responseText.substring(idx + "설명:".length()).trim();
        }
        return responseText.trim();
    }
}
