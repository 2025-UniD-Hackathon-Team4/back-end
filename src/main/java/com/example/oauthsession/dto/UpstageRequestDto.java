package com.example.oauthsession.dto;

import java.util.List;

public class UpstageRequestDto {

    // 사용할 모델 이름 (예: "solar-pro2")
    private String model;

    // 대화 내역 (시스템, 유저, 어시스턴트 메시지 리스트)
    private List<Message> messages;

    // 스트리밍 활성화 여부 (True 설정 시 응답이 조각(Chunk) 단위로 실시간 전송됨)
    private Boolean stream = false;

    // 토큰 생성을 제한하는 최대 토큰 수 (선택 사항)
    private Integer max_tokens;

    // 기본 생성자 (Spring이 JSON을 객체로 변환할 때 사용)
    public UpstageRequestDto() {}

    // 모든 필드를 포함하는 생성자
    public UpstageRequestDto(String model, List<Message> messages, Boolean stream, Integer max_tokens) {
        this.model = model;
        this.messages = messages;
        this.stream = stream;
        this.max_tokens = max_tokens;
    }

    // --- Message Sub-class (Inner Class) ---
    public static class Message {
        private String role; // "user", "system", "assistant"
        private String content; // 메시지 내용

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getter and Setter
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Getter and Setter for UpstageRequestDto
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public Boolean getStream() { return stream; }
    public void setStream(Boolean stream) { this.stream = stream; }
    public Integer getMax_tokens() { return max_tokens; }
    public void setMax_tokens(Integer max_tokens) { this.max_tokens = max_tokens; }
}
