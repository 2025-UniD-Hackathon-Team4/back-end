package com.example.oauthsession.dto;

import java.util.List;

// Upstage API의 /v1/chat/completions 엔드포인트 응답 형식에 맞춘 DTO
public class UpstageResponseDto {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    // Usage 정보는 생략하거나 필요하면 추가할 수 있습니다.

    // 응답에서 최종 생성된 메시지를 추출하기 위해 Choice 클래스 정의
    public static class Choice {
        private int index;
        private UpstageRequestDto.Message message; // 요청 DTO에서 정의된 Message 클래스 재사용
        private String finish_reason;

        // Getter and Setter
        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public UpstageRequestDto.Message getMessage() { return message; }
        public void setMessage(UpstageRequestDto.Message message) { this.message = message; }
        public String getFinish_reason() { return finish_reason; }
        public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }
    }

    // Getter and Setter for UpstageResponseDto
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    public long getCreated() { return created; }
    public void setCreated(long created) { this.created = created; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }
}