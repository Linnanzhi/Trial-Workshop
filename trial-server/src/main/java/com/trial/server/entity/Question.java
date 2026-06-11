package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体
 */
@Data
@TableName(value = "t_question", autoResultMap = true)
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题型: 1-单选 2-多选 3-判断 4-填空 5-简答 */
    private Integer type;

    /** 题干 */
    private String stem;

    /** 选项列表(JSON) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> options;

    /** 正确答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 难度 1-5 */
    private Integer difficulty;

    /** 分类ID */
    private Long categoryId;

    /** 创建者ID */
    private Long userId;

    /** 来源: MANUAL / AI_IMPORT */
    private String source;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 非数据库字段 - 标签列表 */
    @TableField(exist = false)
    private List<Tag> tags;

    /** 非数据库字段 - 分类名称 */
    @TableField(exist = false)
    private String categoryName;
}
