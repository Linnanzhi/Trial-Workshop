package com.trial.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.BusinessException;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.QuestionDTO;
import com.trial.server.entity.*;
import com.trial.server.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目服务
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionTagMapper questionTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final ErrorBookMapper errorBookMapper;

    /**
     * 分页查询题目
     */
    public Page<Question> listQuestions(Integer pageNum, Integer pageSize,
                                        String keyword, Long categoryId,
                                        Integer type, Integer difficulty) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<Question> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId, userId);
        wrapper.eq(categoryId != null, Question::getCategoryId, categoryId);
        wrapper.eq(type != null, Question::getType, type);
        wrapper.eq(difficulty != null, Question::getDifficulty, difficulty);
        wrapper.like(StringUtils.hasText(keyword), Question::getStem, keyword);
        wrapper.orderByDesc(Question::getCreateTime);

        Page<Question> result = questionMapper.selectPage(page, wrapper);

        // 填充标签和分类名称
        for (Question question : result.getRecords()) {
            fillQuestionExtras(question);
        }

        return result;
    }

    /**
     * 获取题目详情
     */
    public Question getQuestionDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        fillQuestionExtras(question);
        return question;
    }

    /**
     * 新增题目
     */
    @Transactional
    public void addQuestion(QuestionDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();

        Question question = new Question();
        question.setType(dto.getType());
        question.setStem(dto.getStem());
        question.setOptions(dto.getOptions());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 3);
        question.setCategoryId(dto.getCategoryId());
        question.setUserId(userId);
        question.setSource(dto.getSource() != null ? dto.getSource() : "MANUAL");
        questionMapper.insert(question);

        // 保存标签关联
        saveQuestionTags(question.getId(), dto.getTagIds());
    }

    /**
     * 批量导入题目（AI识别后的结构化数据）
     */
    @Transactional
    public int batchImport(List<QuestionDTO> questions) {
        Long userId = SecurityUtil.getCurrentUserId();
        int count = 0;
        for (QuestionDTO dto : questions) {
            Question question = new Question();
            question.setType(dto.getType());
            question.setStem(dto.getStem());
            question.setOptions(dto.getOptions());
            question.setAnswer(dto.getAnswer());
            question.setAnalysis(dto.getAnalysis());
            question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 3);
            question.setCategoryId(dto.getCategoryId());
            question.setUserId(userId);
            question.setSource("AI_IMPORT");
            questionMapper.insert(question);
            saveQuestionTags(question.getId(), dto.getTagIds());
            count++;
        }
        return count;
    }

    /**
     * 修改题目
     */
    @Transactional
    public void updateQuestion(QuestionDTO dto) {
        Question question = questionMapper.selectById(dto.getId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        question.setType(dto.getType());
        question.setStem(dto.getStem());
        question.setOptions(dto.getOptions());
        question.setAnswer(dto.getAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setCategoryId(dto.getCategoryId());
        questionMapper.updateById(question);

        // 更新标签关联
        questionTagMapper.deleteByQuestionId(question.getId());
        saveQuestionTags(question.getId(), dto.getTagIds());
    }

    /**
     * 删除题目
     */
    /**
     * 删除题目
     */
    @Transactional
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
        questionTagMapper.deleteByQuestionId(id);
        
        // 删除该题目在错题本中的记录
        LambdaQueryWrapper<ErrorBook> ew = new LambdaQueryWrapper<>();
        ew.eq(ErrorBook::getQuestionId, id);
        errorBookMapper.delete(ew);
    }

    /**
     * 批量删除题目
     */
    @Transactional
    public int batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        Long userId = SecurityUtil.getCurrentUserId();
        int count = 0;
        
        for (Long id : ids) {
            Question question = questionMapper.selectById(id);
            // 验证题目是否属于当前用户
            if (question != null && question.getUserId().equals(userId)) {
                questionMapper.deleteById(id);
                questionTagMapper.deleteByQuestionId(id);
                
                // 删除该题目在错题本中的记录
                LambdaQueryWrapper<ErrorBook> ew = new LambdaQueryWrapper<>();
                ew.eq(ErrorBook::getQuestionId, id);
                errorBookMapper.delete(ew);
                
                count++;
            }
        }
        
        return count;
    }

    /**
     * 批量更新分类
     */
    @Transactional
    public int batchUpdateCategory(List<Long> ids, Long categoryId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        Long userId = SecurityUtil.getCurrentUserId();
        int count = 0;
        
        for (Long id : ids) {
            Question question = questionMapper.selectById(id);
            // 验证题目是否属于当前用户
            if (question != null && question.getUserId().equals(userId)) {
                question.setCategoryId(categoryId);
                questionMapper.updateById(question);
                count++;
            }
        }
        
        return count;
    }

    /**
     * 批量更新标签
     */
    @Transactional
    public int batchUpdateTags(List<Long> ids, List<Long> tagIds) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        Long userId = SecurityUtil.getCurrentUserId();
        int count = 0;
        
        for (Long id : ids) {
            Question question = questionMapper.selectById(id);
            // 验证题目是否属于当前用户
            if (question != null && question.getUserId().equals(userId)) {
                // 删除旧的标签关联
                questionTagMapper.deleteByQuestionId(id);
                
                // 添加新的标签关联
                if (tagIds != null && !tagIds.isEmpty()) {
                    for (Long tagId : tagIds) {
                        QuestionTag qt = new QuestionTag();
                        qt.setQuestionId(id);
                        qt.setTagId(tagId);
                        questionTagMapper.insert(qt);
                    }
                }
                
                count++;
            }
        }
        
        return count;
    }

    /**
     * 获取用户题库统计
     */
    public java.util.Map<String, Object> getQuestionStats() {
        Long userId = SecurityUtil.getCurrentUserId();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId, userId);

        stats.put("total", questionMapper.selectCount(wrapper));

        // 各题型统计
        for (int i = 1; i <= 5; i++) {
            LambdaQueryWrapper<Question> tw = new LambdaQueryWrapper<>();
            tw.eq(Question::getUserId, userId).eq(Question::getType, i);
            stats.put("type" + i, questionMapper.selectCount(tw));
        }

        return stats;
    }

    // ===== 私有方法 =====

    private void saveQuestionTags(Long questionId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                QuestionTag qt = new QuestionTag();
                qt.setQuestionId(questionId);
                qt.setTagId(tagId);
                questionTagMapper.insert(qt);
            }
        }
    }

    private void fillQuestionExtras(Question question) {
        // 填充标签
        List<Long> tagIds = questionTagMapper.selectTagIdsByQuestionId(question.getId());
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            question.setTags(tags);
        } else {
            question.setTags(new ArrayList<>());
        }
        // 填充分类名称
        if (question.getCategoryId() != null) {
            Category category = categoryMapper.selectById(question.getCategoryId());
            if (category != null) {
                question.setCategoryName(category.getName());
            }
        }
    }
}
