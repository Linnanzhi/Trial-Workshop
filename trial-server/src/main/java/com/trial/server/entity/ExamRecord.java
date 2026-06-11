package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 考试记录实体
 */
@Data
@TableName(value = "t_exam_record", autoResultMap = true)
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long examId;

    private Integer score;

    private Integer totalScore;

    private Integer correctCount;

    private Integer wrongCount;

    /** 状态: 0-考试中 1-已交卷 2-已批改 */
    private Integer status;

    /** 答题详情 JSON */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> answers;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 实际用时(秒) */
    private Integer duration;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
