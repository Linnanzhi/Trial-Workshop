package com.trial.server.dto;

import lombok.Data;

import java.util.List;

/**
 * 组卷请求 DTO
 */
@Data
public class ExamCreateDTO {

    /** 试卷标题 */
    private String title;

    /** 试卷描述 */
    private String description;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 考试时长(分钟) */
    private Integer duration;

    /** 是否打乱题序 */
    private Integer shuffleQuestion;

    /** 是否打乱选项 */
    private Integer shuffleOption;

    /** 错题自动收录 */
    private Integer autoCollectError;

    // ===== 手动组卷字段 =====
    /** 手动选择的题目ID列表，null则为自动组卷 */
    private List<Long> questionIds;

    /** 每题分值（手动组卷时） */
    private Integer scorePerQuestion;

    // ===== 自动组卷字段 =====
    /** 从哪个分类抽题 */
    private Long categoryId;

    /** 从哪些标签抽题 */
    private List<Long> tagIds;

    /** 难度范围 */
    private Integer minDifficulty;
    private Integer maxDifficulty;

    /** 自动抽取题目数量 */
    private Integer autoCount;
}
