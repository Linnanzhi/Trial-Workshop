package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 试卷-题目关联实体
 */
@Data
@TableName("t_exam_question")
public class ExamQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long questionId;

    private Integer score;

    private Integer orderSeq;
}
