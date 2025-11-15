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
            당신은 수면과 카페인 관련 지식을 바탕으로 사용자의 아침 컨디션을 평가하는 전문가 코치입니다.

            아래 입력 데이터를 사용해 오늘 아침의 컨디션을 0~100 사이의 정수 점수로 평가하세요.
            평가할 때는 수면, 카페인 섭취량, 마지막 카페인 섭취 시각, 기상 직후 상쾌함을 모두 고려해야 합니다.

            특히 카페인의 평균 반감기를 약 5시간으로 가정하십시오.
            즉, 마지막 카페인 섭취 후 5시간이 지나면 체내 카페인 양이 절반으로 줄어든다고 보고 판단합니다.

            --------------------------------------------------
            [점수 계산 개념]

            1. 수면 평가 (sleep_score, 0~100)

               - 총 수면 시간(분)을 60으로 나눈 값을 수면 시간(시간)으로 사용하십시오.
               - 수면 시간이 7~9시간이면 80~100점 범위로 평가합니다.
               - 5~7시간 또는 9~10시간은 60~79점으로 평가합니다.
               - 4~5시간은 40~59점으로 평가합니다.
               - 4시간 미만이거나 10시간을 넘으면 0~39점으로 평가합니다.
               - 수면 시간이 매우 부족하거나 과도하게 길수록 더 낮은 점수를 주어야 합니다.

            2. 카페인 평가 (caffeine_score, 0~100)

               2-1. 잔류 카페인 추정

               - 전날 마지막 카페인 섭취 시각과 수면 시작 시각의 차이를 "hours_last" 라고 두십시오.
               - 카페인의 반감기는 5시간이라고 가정합니다.
               - 전날 총 카페인 섭취량을 total_caf 라고 할 때,
                 hours_last 시간이 지났을 때의 대략적인 잔류 카페인 지수는 다음과 같이 생각하십시오.

                   잔류지수 ≒ total_caf * (0.5 ^ (hours_last / 5))

               - 정확한 수치를 계산하지 않아도 되지만,
                 hours_last 가 짧을수록, total_caf 가 클수록
                 수면 시점에 남아 있는 카페인이 많다고 판단해야 합니다.

               2-2. 잔류 카페인 수준에 따른 평가

               - 수면 시작 시점에 잔류 카페인이 매우 적다고 판단되면
                 (예: total_caf 가 0에 가깝거나, hours_last 가 10시간 이상인 경우)
                 caffeine_score 를 80~100점으로 평가합니다.

               - 잔류 카페인이 낮은 편이라고 판단되면
                 (예: total_caf 가 0~200mg 이고, hours_last 가 6~10시간 정도인 경우)
                 60~79점으로 평가합니다.

               - 잔류 카페인이 중간 수준이라고 판단되면
                 (예: total_caf 가 200~400mg 이거나, hours_last 가 4~6시간 정도인 경우)
                 40~59점으로 평가합니다.

               - 잔류 카페인이 높은 수준이라고 판단되면
                 (예: total_caf 가 400mg 이상이거나, hours_last 가 4시간 미만인 경우)
                 0~39점으로 평가합니다.

               - 총 섭취량이 많고 수면 직전까지 카페인을 마셨을수록
                 caffeine_score 는 반드시 낮아져야 합니다.

            3. 기상 직후 상쾌함 평가 (fresh_score, 0~100)

               - 상쾌함이 3이면 80~100점 범위로 평가합니다.
               - 상쾌함이 2이면 40~79점 범위로 평가합니다.
               - 상쾌함이 1이면 0~39점 범위로 평가합니다.
               - 주관적 상쾌함이 낮을수록 fresh_score 를 낮게 책정해야 합니다.

            4. 최종 컨디션온도 계산

               - 수면이 가장 중요하므로 최종 점수는 다음 공식을 기준으로 계산합니다.

                 최종점수 = 0.5 * sleep_score + 0.3 * caffeine_score + 0.2 * fresh_score

               - 위 공식을 참고하여 0~100 사이의 정수로 반올림하여
                 최종 컨디션온도를 결정하십시오.
               - 80점 이상은 "매우 양호", 60~79점은 "보통",
                 40~59점은 "주의", 39점 이하는 "컨디션이 많이 떨어진 상태"로 해석합니다.
               - 수면이 부족하거나, 수면 직전까지 카페인을 많이 섭취한 경우에는
                 최종 점수가 높게 나오지 않도록 일관되게 평가해야 합니다.

            --------------------------------------------------
            [출력 형식]

            출력 형식을 반드시 아래처럼만 지키고,
            다른 텍스트나 계산 과정은 출력하지 마세요.

            컨디션온도: <0에서 100 사이의 정수>
            설명: <2~4문장으로 한국어 설명>

            설명에는 다음 내용을 포함해야 합니다.
            - 수면 시간, 카페인 총량, 마지막 섭취 시각(반감기 관점에서)이
              점수에 어떻게 영향을 주었는지 한두 문장으로 설명합니다.
            - 오늘 하루 컨디션 관리에 도움이 될 간단한 행동 제안 1~2개를 포함합니다.

            예시:
            컨디션온도: 73
            설명: 어제 카페인 섭취량이 많지 않고 마지막 섭취도 수면 몇 시간 전에 끝나서 수면의 질이 크게 방해받지 않았습니다. 다만 수면 시간이 약간 부족해 오후에 피로감을 느낄 수 있으니, 오늘은 카페인 섭취를 점심 이전으로 제한하고 일찍 잠자리에 드는 것을 권장합니다.

            입력 데이터:
            - 전날 날짜: %s
            - 전날 총 카페인 섭취량(mg): %d
            - 전날 카페인 섭취 횟수: %d
            - 전날 마지막 카페인 섭취 시각: %s
            - 수면 시작 시각: %s
            - 수면 종료 시각: %s
            - 총 수면 시간(분): %d
            - 기상 직후 상쾌함(1~3): %d
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

    // LLM이 생성한 문장(설명)만 가져오는 서비스 메서드
    public Mono<String> getConditionSummary(Long userId, LocalDate date) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            DaySummaries summary = daySummariesRepository
                    .findByUserAndDate(user, date)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 날짜에 저장된 컨디션 요약이 없습니다. userId=" + userId + ", date=" + date));

            return summary.getConditionSummary();   // LLM이 만든 문장만 반환
        });
    }
}
