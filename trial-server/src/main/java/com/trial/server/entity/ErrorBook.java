package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本实体
 */
@Data
@TableName("t_error_book")
public class ErrorBook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private Integer errorCount;

    private LocalDateTime lastErrorTime;

    /** 下次复习时间(艾宾浩斯) */
    private LocalDateTime nextReviewTime;

    /** 复习阶段 */
    private Integer reviewStage;

    /** 是否已掌握 */
    private Integer mastered;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
