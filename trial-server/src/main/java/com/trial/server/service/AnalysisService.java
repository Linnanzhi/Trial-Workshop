package com.trial.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.config.SecurityUtil;
import com.trial.server.entity.ErrorBook;
import com.trial.server.entity.ExamRecord;
import com.trial.server.entity.Question;
import com.trial.server.mapper.ErrorBookMapper;
import com.trial.server.mapper.ExamRecordMapper;
import com.trial.server.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学情分析服务
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ExamRecordMapper examRecordMapper;
    private final ErrorBookMapper errorBookMapper;
    private final QuestionMapper questionMapper;

    /**
     * 获取仪表盘总览数据
     */
    public Map<String, Object> getDashboard() {
        Long userId = SecurityUtil.getCurrentUserId();
        Map<String, Object> dashboard = new HashMap<>();

        // 考试总次数
        Long examCount = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 2)
        );
        dashboard.put("examCount", examCount);

        // 平均分
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 2)
                        .orderByDesc(ExamRecord::getCreateTime)
        );
        double avgScore = records.stream()
                .mapToInt(r -> r.getTotalScore() > 0 ? (r.getScore() * 100 / r.getTotalScore()) : 0)
                .average().orElse(0);
        dashboard.put("avgScore", Math.round(avgScore));

        // 总正确率
        int totalCorrect = records.stream().mapToInt(ExamRecord::getCorrectCount).sum();
        int totalWrong = records.stream().mapToInt(ExamRecord::getWrongCount).sum();
        int total = totalCorrect + totalWrong;
        dashboard.put("correctRate", total > 0 ? Math.round(totalCorrect * 100.0 / total) : 0);

        // 错题本待复习数
        Long reviewCount = errorBookMapper.selectCount(
                new LambdaQueryWrapper<ErrorBook>()
                        .eq(ErrorBook::getUserId, userId)
                        .eq(ErrorBook::getMastered, 0)
                        .le(ErrorBook::getNextReviewTime, LocalDateTime.now())
        );
        dashboard.put("reviewCount", reviewCount);

        // 总练习时长(分钟)
        int totalDuration = records.stream().mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0).sum();
        dashboard.put("totalDuration", totalDuration / 60);

        // 最近10次考试成绩趋势
        List<Map<String, Object>> trend = records.stream()
                .limit(10)
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", r.getCreateTime());
                    m.put("score", r.getTotalScore() > 0 ? (r.getScore() * 100 / r.getTotalScore()) : 0);
                    return m;
                })
                .collect(Collectors.toList());
        Collections.reverse(trend);
        dashboard.put("trend", trend);

        return dashboard;
    }

    /**
     * 获取错题本列表
     */
    public Page<Map<String, Object>> getErrorBookList(Integer pageNum, Integer pageSize, Integer mastered) {
        Long userId = SecurityUtil.getCurrentUserId();

        LambdaQueryWrapper<ErrorBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorBook::getUserId, userId);
        if (mastered != null) {
            wrapper.eq(ErrorBook::getMastered, mastered);
        }
        wrapper.orderByDesc(ErrorBook::getLastErrorTime);

        Page<ErrorBook> page = errorBookMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 转化为包含题目信息的返回
        List<Map<String, Object>> records = page.getRecords().stream().map(eb -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", eb.getId());
            item.put("errorCount", eb.getErrorCount());
            item.put("lastErrorTime", eb.getLastErrorTime());
            item.put("nextReviewTime", eb.getNextReviewTime());
            item.put("reviewStage", eb.getReviewStage());
            item.put("mastered", eb.getMastered());

            Question q = questionMapper.selectById(eb.getQuestionId());
            if (q != null) {
                item.put("questionId", q.getId());
                item.put("stem", q.getStem());
                item.put("type", q.getType());
                item.put("answer", q.getAnswer());
                item.put("analysis", q.getAnalysis());
            }
            return item;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 获取今日待复习的错题（艾宾浩斯）
     */
    public List<Map<String, Object>> getTodayReview() {
        Long userId = SecurityUtil.getCurrentUserId();

        List<ErrorBook> errors = errorBookMapper.selectList(
                new LambdaQueryWrapper<ErrorBook>()
                        .eq(ErrorBook::getUserId, userId)
                        .eq(ErrorBook::getMastered, 0)
                        .le(ErrorBook::getNextReviewTime, LocalDateTime.now())
                        .orderByAsc(ErrorBook::getNextReviewTime)
                        .last("LIMIT 20")
        );

        return errors.stream().map(eb -> {
            Map<String, Object> item = new HashMap<>();
            item.put("errorBookId", eb.getId());
            Question q = questionMapper.selectById(eb.getQuestionId());
            if (q != null) {
                item.put("questionId", q.getId());
                item.put("stem", q.getStem());
                item.put("type", q.getType());
                item.put("options", q.getOptions());
                item.put("answer", q.getAnswer());
                item.put("analysis", q.getAnalysis());
            }
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 错题复习反馈 - 标记掌握或推进到下一阶段
     */
    public void reviewFeedback(Long errorBookId, boolean mastered) {
        ErrorBook eb = errorBookMapper.selectById(errorBookId);
        if (eb == null) return;

        if (mastered) {
            eb.setMastered(1);
        } else {
            // 艾宾浩斯遗忘曲线间隔：1天, 2天, 4天, 7天, 15天, 30天
            int[] intervals = {1, 2, 4, 7, 15, 30};
            int stage = eb.getReviewStage() + 1;
            if (stage >= intervals.length) stage = intervals.length - 1;
            eb.setReviewStage(stage);
            eb.setNextReviewTime(LocalDateTime.now().plusDays(intervals[stage]));
        }
        errorBookMapper.updateById(eb);
    }

    /**
     * 获取能力雷达图数据（按知识点标签维度统计正确率）
     */
    public Map<String, Object> getRadarData() {
        Long userId = SecurityUtil.getCurrentUserId();

        // 获取用户所有已批改的考试记录
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, 2)
        );

        // 按题型统计正确率
        Map<String, int[]> typeStats = new HashMap<>();
        String[] typeNames = {"", "单选题", "多选题", "判断题", "填空题", "简答题"};

        for (ExamRecord record : records) {
            if (record.getAnswers() == null) continue;
            for (Map<String, Object> answer : record.getAnswers()) {
                Long qId = Long.valueOf(answer.get("questionId").toString());
                Question q = questionMapper.selectById(qId);
                if (q == null) continue;

                String typeName = q.getType() >= 1 && q.getType() <= 5 ? typeNames[q.getType()] : "其他";
                typeStats.computeIfAbsent(typeName, k -> new int[]{0, 0});
                int[] stats = typeStats.get(typeName);
                stats[1]++; // 总数
                if (Boolean.TRUE.equals(answer.get("isCorrect"))) {
                    stats[0]++; // 正确数
                }
            }
        }

        // 转化为雷达图数据
        List<Map<String, Object>> radarItems = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : typeStats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("total", entry.getValue()[1]);
            item.put("correct", entry.getValue()[0]);
            item.put("rate", entry.getValue()[1] > 0
                    ? Math.round(entry.getValue()[0] * 100.0 / entry.getValue()[1]) : 0);
            radarItems.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("radar", radarItems);
        return result;
    }
}
