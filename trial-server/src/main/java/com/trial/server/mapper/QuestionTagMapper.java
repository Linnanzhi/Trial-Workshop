package com.trial.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trial.server.entity.QuestionTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionTagMapper extends BaseMapper<QuestionTag> {

    @Delete("DELETE FROM t_question_tag WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);

    @Select("SELECT tag_id FROM t_question_tag WHERE question_id = #{questionId}")
    List<Long> selectTagIdsByQuestionId(@Param("questionId") Long questionId);
}
