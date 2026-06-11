package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 题目-标签关联实体
 */
@Data
@TableName("t_question_tag")
public class QuestionTag {

    private Long questionId;

    private Long tagId;
}
