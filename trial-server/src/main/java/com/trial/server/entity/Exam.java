package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@TableName("t_exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Integer totalScore;

    private Integer passScore;

    /** 考试时长(分钟) */
    private Integer duration;

    private Integer shuffleQuestion;

    private Integer shuffleOption;

    private Integer autoCollectError;

    private Integer questionCount;

    private String shareCode;

    private Long userId;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
