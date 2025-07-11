package com.election.polling.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class PollResponse {

    private Long id;
    private String question;
    private LocalDateTime expiryDateTime;
    private List<OptionResponse> options = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public LocalDateTime getExpiryDateTime() {
        return expiryDateTime;
    }

    public void setExpiryDateTime(LocalDateTime expiryDateTime) {
        this.expiryDateTime = expiryDateTime;
    }

    public List<OptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<OptionResponse> options) {
        this.options = options;
    }

    public PollResponse(Long id, String question, LocalDateTime expiryDateTime, List<OptionResponse> options) {
        this.id = id;
        this.question = question;
        this.expiryDateTime = expiryDateTime;
        this.options = options;
    }
}
