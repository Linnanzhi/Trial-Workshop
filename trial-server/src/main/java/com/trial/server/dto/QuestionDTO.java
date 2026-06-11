package com.trial.server.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目请求 DTO
 */
@Data
public class QuestionDTO {

    private Long id;

    /** 题型: 1-单选 2-多选 3-判断 4-填空 5-简答 */
    private Integer type;

    /** 题干 */
    private String stem;

    /** 选项列表 */
    private List<String> options;

    /** 正确答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 难度 */
    private Integer difficulty;

    /** 分类ID */
    private Long categoryId;

    /** 标签ID列表 */
    private List<Long> tagIds;

    /** 来源 */
    private String source;
}
