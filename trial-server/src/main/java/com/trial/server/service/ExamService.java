package com.trial.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.BusinessException;
import com.trial.server.common.JudgmentAnswerUtil;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.ExamCreateDTO;
import com.trial.server.dto.SubmitExamDTO;
import com.trial.server.entity.*;
import com.trial.server.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务
 */
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final QuestionMapper questionMapper;
    private final ErrorBookMapper errorBookMapper;

    /**
     * 创建试卷（支持手动/自动组卷）
     */
    @Transactional
    public Long createExam(ExamCreateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<Long> questionIds;

        if (dto.getQuestionIds() != null && !dto.getQuestionIds().isEmpty()) {
            // 手动组卷
            questionIds = dto.getQuestionIds();
        } else {
            // 自动组卷 - 根据条件随机抽题
            questionIds = autoSelectQuestions(userId, dto);
        }

        if (questionIds.isEmpty()) {
            throw new BusinessException("没有符合条件的题目，无法组卷");
        }

        // 创建试卷
        Exam exam = new Exam();
        exam.setTitle(dto.getTitle());
        exam.setDescription(dto.getDescription());
        exam.setDuration(dto.getDuration() != null ? dto.getDuration() : 60);
        exam.setPassScore(dto.getPassScore() != null ? dto.getPassScore() : 60);
        exam.setShuffleQuestion(dto.getShuffleQuestion() != null ? dto.getShuffleQuestion() : 0);
        exam.setShuffleOption(dto.getShuffleOption() != null ? dto.getShuffleOption() : 0);
        exam.setAutoCollectError(dto.getAutoCollectError() != null ? dto.getAutoCollectError() : 1);
        exam.setQuestionCount(questionIds.size());
        exam.setUserId(userId);
        exam.setStatus(1);

        // 计算总分
        int scorePerQ = dto.getScorePerQuestion() != null ? dto.getScorePerQuestion() : 5;
        exam.setTotalScore(questionIds.size() * scorePerQ);

        examMapper.insert(exam);

        // 保存试卷题目关联
        for (int i = 0; i < questionIds.size(); i++) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExamId(exam.getId());
            eq.setQuestionId(questionIds.get(i));
            eq.setScore(scorePerQ);
            eq.setOrderSeq(i + 1);
            examQuestionMapper.insert(eq);
        }

        return exam.getId();
    }

    /**
     * 获取用户的试卷列表
     */
    public Page<Exam> listExams(Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        return examMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getUserId, userId)
                        .orderByDesc(Exam::getCreateTime)
        );
    }

    /**
     * 获取试卷详情（含题目列表）
     */
    public Map<String, Object> getExamDetail(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }

        // 查询关联题目
        List<ExamQuestion> eqs = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>()
                        .eq(ExamQuestion::getExamId, examId)
                        .orderByAsc(ExamQuestion::getOrderSeq)
        );

        List<Map<String, Object>> questions = new ArrayList<>();
        for (ExamQuestion eq : eqs) {
            Question q = questionMapper.selectById(eq.getQuestionId());
            if (q != null) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("type", q.getType());
                qMap.put("stem", q.getStem());
                qMap.put("options", q.getOptions());
                qMap.put("difficulty", q.getDifficulty());
                qMap.put("score", eq.getScore());
                qMap.put("orderSeq", eq.getOrderSeq());
                // 不暴露答案和解析
                questions.add(qMap);
            }
        }

        // 如果需要打乱题序
        if (exam.getShuffleQuestion() == 1) {
            Collections.shuffle(questions);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("questions", questions);
        return result;
    }

    /**
     * 开始考试 - 创建考试记录
     */
    public Long startExam(Long examId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }

        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setExamId(examId);
        record.setTotalScore(exam.getTotalScore());
        record.setStatus(0); // 考试中
        record.setStartTime(LocalDateTime.now());
        examRecordMapper.insert(record);

        return record.getId();
    }

    /**
     * 自动保存答题进度
     */
    public void saveProgress(Long recordId, List<Map<String, Object>> answers) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record != null && record.getStatus() == 0) {
            record.setAnswers(answers);
            examRecordMapper.updateById(record);
        }
    }

    /**
     * 交卷并自动判分
     */
    @Transactional
    public Map<String, Object> submitExam(SubmitExamDTO dto) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该考试已提交");
        }

        Exam exam = examMapper.selectById(record.getExamId());
        Long userId = record.getUserId();

        // 获取试卷题目
        List<ExamQuestion> eqs = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, exam.getId())
        );
        Map<Long, Integer> scoreMap = eqs.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, ExamQuestion::getScore));

        int totalScore = 0;
        int correctCount = 0;
        int wrongCount = 0;
        List<Map<String, Object>> answerDetails = new ArrayList<>();

        for (SubmitExamDTO.AnswerItem item : dto.getAnswers()) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question == null) continue;

            boolean isCorrect = checkAnswer(question, item.getUserAnswer());
            int qScore = scoreMap.getOrDefault(item.getQuestionId(), 0);

            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", item.getQuestionId());
            detail.put("userAnswer", item.getUserAnswer());
            detail.put("correctAnswer", question.getAnswer());
            detail.put("isCorrect", isCorrect);
            detail.put("score", isCorrect ? qScore : 0);
            detail.put("analysis", question.getAnalysis());
            answerDetails.add(detail);

            if (isCorrect) {
                totalScore += qScore;
                correctCount++;
            } else {
                wrongCount++;
                // 错题自动收录
                if (exam.getAutoCollectError() == 1) {
                    collectToErrorBook(userId, item.getQuestionId());
                }
            }
        }

        // 更新考试记录
        record.setScore(totalScore);
        record.setCorrectCount(correctCount);
        record.setWrongCount(wrongCount);
        record.setStatus(2); // 已批改
        record.setAnswers(answerDetails);
        record.setEndTime(LocalDateTime.now());
        record.setDuration((int) Duration.between(record.getStartTime(), record.getEndTime()).getSeconds());
        examRecordMapper.updateById(record);

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("score", totalScore);
        result.put("totalScore", record.getTotalScore());
        result.put("correctCount", correctCount);
        result.put("wrongCount", wrongCount);
        result.put("passed", totalScore >= exam.getPassScore());
        result.put("duration", record.getDuration());
        result.put("details", answerDetails);
        return result;
    }

    /**
     * 获取考试记录列表
     */
    public Page<ExamRecord> listRecords(Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        return examRecordMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .orderByDesc(ExamRecord::getCreateTime)
        );
    }

    /**
     * 获取考试记录详情
     */
    public ExamRecord getRecordDetail(Long recordId) {
        return examRecordMapper.selectById(recordId);
    }

    /**
     * 删除试卷
     */
    @Transactional
    public void deleteExam(Long examId) {
        examMapper.deleteById(examId);
        examQuestionMapper.delete(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, examId)
        );
    }
    
    /**
     * 生成并获取试卷分享码
     */
    public String generateShareCode(Long examId) {
        Long userId = SecurityUtil.getCurrentUserId();
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("试卷不存在");
        }
        if (!exam.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此试卷");
        }
        
        // 如果已经有分享码则直接返回
        if (StringUtils.hasText(exam.getShareCode())) {
            return exam.getShareCode();
        }
        
        // 生成随机分享码 (6位大写字母+数字)
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
        
        // 确保唯一 (理论上极低概率重复，可加判断)
        while (examMapper.selectCount(new LambdaQueryWrapper<Exam>().eq(Exam::getShareCode, code)) > 0) {
            code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
        }
        
        exam.setShareCode(code);
        examMapper.updateById(exam);
        
        return code;
    }

    /**
     * 通过分享码导入试卷
     */
    @Transactional
    public void importExamByShareCode(String shareCode) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        if (!StringUtils.hasText(shareCode)) {
            throw new BusinessException("分享码不能为空");
        }
        
        // 查找原始试卷（逻辑删除的试卷查不到，实现了删除即失效）
        Exam originalExam = examMapper.selectOne(
                new LambdaQueryWrapper<Exam>().eq(Exam::getShareCode, shareCode)
        );
        
        if (originalExam == null) {
            throw new BusinessException("分享码无效或试卷已被删除");
        }
        
        if (originalExam.getUserId().equals(currentUserId)) {
            throw new BusinessException("不能导入自己的试卷");
        }
        
        // 1. 克隆试卷信息 (不包含shareCode)
        Exam newExam = new Exam();
        newExam.setTitle(originalExam.getTitle() + " (共享导入)");
        newExam.setDescription(originalExam.getDescription());
        newExam.setTotalScore(originalExam.getTotalScore());
        newExam.setPassScore(originalExam.getPassScore());
        newExam.setDuration(originalExam.getDuration());
        newExam.setShuffleQuestion(originalExam.getShuffleQuestion());
        newExam.setShuffleOption(originalExam.getShuffleOption());
        newExam.setAutoCollectError(originalExam.getAutoCollectError());
        newExam.setQuestionCount(originalExam.getQuestionCount());
        newExam.setUserId(currentUserId);
        newExam.setStatus(1);
        examMapper.insert(newExam);
        
        // 2. 深度克隆绑定的所有题目，插入到当前用户的题库
        List<ExamQuestion> originalEqs = examQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamQuestion>().eq(ExamQuestion::getExamId, originalExam.getId())
        );
        
        for (ExamQuestion oldEq : originalEqs) {
            Question oldQuestion = questionMapper.selectById(oldEq.getQuestionId());
            if (oldQuestion == null) continue;
            
            // 复制题目
            Question newQuestion = new Question();
            newQuestion.setType(oldQuestion.getType());
            newQuestion.setStem(oldQuestion.getStem());
            newQuestion.setOptions(oldQuestion.getOptions());
            newQuestion.setAnswer(oldQuestion.getAnswer());
            newQuestion.setAnalysis(oldQuestion.getAnalysis());
            newQuestion.setDifficulty(oldQuestion.getDifficulty());
            newQuestion.setUserId(currentUserId); // 归属当前用户
            newQuestion.setCategoryId(null); // 可以考虑归到默认分类，这里先填null
            newQuestion.setSource("SHARE_IMPORT");
            questionMapper.insert(newQuestion);
            
            // 绑定新试卷题库
            ExamQuestion newEq = new ExamQuestion();
            newEq.setExamId(newExam.getId());
            newEq.setQuestionId(newQuestion.getId());
            newEq.setScore(oldEq.getScore());
            newEq.setOrderSeq(oldEq.getOrderSeq());
            examQuestionMapper.insert(newEq);
        }
    }

    // ===== 私有方法 =====

    /**
     * 自动抽题
     */
    private List<Long> autoSelectQuestions(Long userId, ExamCreateDTO dto) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId, userId);
        wrapper.eq(dto.getCategoryId() != null, Question::getCategoryId, dto.getCategoryId());
        wrapper.ge(dto.getMinDifficulty() != null, Question::getDifficulty, dto.getMinDifficulty());
        wrapper.le(dto.getMaxDifficulty() != null, Question::getDifficulty, dto.getMaxDifficulty());

        // 标签筛选
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Long> qIds = questionMapper.selectQuestionIdsByTagIds(dto.getTagIds());
            if (qIds.isEmpty()) return new ArrayList<>();
            wrapper.in(Question::getId, qIds);
        }

        List<Question> candidates = questionMapper.selectList(wrapper);
        List<Long> ids = candidates.stream().map(Question::getId).collect(Collectors.toList());

        // 随机抽取指定数量
        int count = dto.getAutoCount() != null ? dto.getAutoCount() : 10;
        Collections.shuffle(ids);
        return ids.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * 判断答案是否正确
     */
    private boolean checkAnswer(Question question, String userAnswer) {
        if (userAnswer == null || question.getAnswer() == null) return false;
        
        // 判断题（type=3）使用标准化答案比较
        if (question.getType() == 3) {
            return JudgmentAnswerUtil.compareJudgmentAnswer(question.getAnswer(), userAnswer);
        }
        
        // 其他题型：去除首尾空白并忽略大小写比较
        return question.getAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }

    /**
     * 错题收录到错题本
     */
    private void collectToErrorBook(Long userId, Long questionId) {
        ErrorBook existing = errorBookMapper.selectOne(
                new LambdaQueryWrapper<ErrorBook>()
                        .eq(ErrorBook::getUserId, userId)
                        .eq(ErrorBook::getQuestionId, questionId)
        );
        if (existing != null) {
            existing.setErrorCount(existing.getErrorCount() + 1);
            existing.setLastErrorTime(LocalDateTime.now());
            existing.setMastered(0);
            // 艾宾浩斯复习间隔: 1天, 2天, 4天, 7天, 15天
            existing.setReviewStage(0);
            existing.setNextReviewTime(LocalDateTime.now().plusDays(1));
            errorBookMapper.updateById(existing);
        } else {
            ErrorBook errorBook = new ErrorBook();
            errorBook.setUserId(userId);
            errorBook.setQuestionId(questionId);
            errorBook.setErrorCount(1);
            errorBook.setLastErrorTime(LocalDateTime.now());
            errorBook.setReviewStage(0);
            errorBook.setNextReviewTime(LocalDateTime.now().plusDays(1));
            errorBook.setMastered(0);
            errorBookMapper.insert(errorBook);
        }
    }
}
