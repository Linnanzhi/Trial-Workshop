package com.trial.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 交卷请求 DTO
 */
@Data
public class SubmitExamDTO {

    /** 考试记录ID */
    private Long recordId;

    /** 答题列表 */
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        /** 题目ID */
        private Long questionId;

        /** 用户作答 */
        private String userAnswer;
    }
}
