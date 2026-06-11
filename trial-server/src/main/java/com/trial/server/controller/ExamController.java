package com.trial.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.Result;
import com.trial.server.dto.ExamCreateDTO;
import com.trial.server.dto.SubmitExamDTO;
import com.trial.server.entity.Exam;
import com.trial.server.entity.ExamRecord;
import com.trial.server.service.ExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考试控制器
 */
@Api(tags = "灵活考试工坊")
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @ApiOperation("创建试卷(手动/自动组卷)")
    @PostMapping
    public Result<Long> createExam(@RequestBody ExamCreateDTO dto) {
        Long examId = examService.createExam(dto);
        return Result.success("组卷成功", examId);
    }

    @ApiOperation("获取试卷列表")
    @GetMapping
    public Result<Page<Exam>> listExams(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(examService.listExams(pageNum, pageSize));
    }

    @ApiOperation("获取试卷详情(含题目)")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getExamDetail(@PathVariable Long id) {
        return Result.success(examService.getExamDetail(id));
    }

    @ApiOperation("开始考试")
    @PostMapping("/{id}/start")
    public Result<Long> startExam(@PathVariable Long id) {
        Long recordId = examService.startExam(id);
        return Result.success("考试开始", recordId);
    }

    @ApiOperation("自动保存答题进度")
    @PostMapping("/records/{recordId}/save")
    public Result<?> saveProgress(@PathVariable Long recordId,
                                   @RequestBody List<Map<String, Object>> answers) {
        examService.saveProgress(recordId, answers);
        return Result.success("保存成功");
    }

    @ApiOperation("交卷")
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitExam(@RequestBody SubmitExamDTO dto) {
        return Result.success("交卷成功", examService.submitExam(dto));
    }

    @ApiOperation("考试记录列表")
    @GetMapping("/records")
    public Result<Page<ExamRecord>> listRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(examService.listRecords(pageNum, pageSize));
    }

    @ApiOperation("考试记录详情")
    @GetMapping("/records/{recordId}")
    public Result<ExamRecord> getRecordDetail(@PathVariable Long recordId) {
        return Result.success(examService.getRecordDetail(recordId));
    }

    @ApiOperation("删除试卷")
    @DeleteMapping("/{id}")
    public Result<?> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return Result.success("删除成功");
    }
    
    @ApiOperation("获取试卷分享码")
    @PostMapping("/{id}/share")
    public Result<String> shareExam(@PathVariable Long id) {
        return Result.success("生成分享码成功", examService.generateShareCode(id));
    }
    
    @ApiOperation("通过分享码导入试卷")
    @PostMapping("/import")
    public Result<?> importExam(@RequestParam("shareCode") String shareCode) {
        examService.importExamByShareCode(shareCode);
        return Result.success("试卷导入成功");
    }
}
