package com.colacode.interview.application.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StartInterviewReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String engineType;

    private List<KeywordItemDTO> keywords;

    @Data
    public static class KeywordItemDTO implements Serializable {
        private String keyWord;
        private Long categoryId;
        private Long labelId;
    }
}
