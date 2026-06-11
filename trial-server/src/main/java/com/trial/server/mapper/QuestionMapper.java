package com.trial.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trial.server.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 根据标签ID列表查询题目ID
     */
    @Select("<script>" +
            "SELECT DISTINCT question_id FROM t_question_tag WHERE tag_id IN " +
            "<foreach collection='tagIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectQuestionIdsByTagIds(@Param("tagIds") List<Long> tagIds);
}
