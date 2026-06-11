package com.trial.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.Result;
import com.trial.server.service.AnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学情分析控制器
 */
@Api(tags = "深度学情分析")
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @ApiOperation("仪表盘总览数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(analysisService.getDashboard());
    }

    @ApiOperation("错题本列表")
    @GetMapping("/error-book")
    public Result<Page<Map<String, Object>>> getErrorBook(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer mastered) {
        return Result.success(analysisService.getErrorBookList(pageNum, pageSize, mastered));
    }

    @ApiOperation("今日待复习错题(艾宾浩斯)")
    @GetMapping("/today-review")
    public Result<List<Map<String, Object>>> getTodayReview() {
        return Result.success(analysisService.getTodayReview());
    }

    @ApiOperation("错题复习反馈")
    @PostMapping("/review-feedback")
    public Result<?> reviewFeedback(@RequestParam Long errorBookId,
                                     @RequestParam boolean mastered) {
        analysisService.reviewFeedback(errorBookId, mastered);
        return Result.success("反馈成功");
    }

    @ApiOperation("能力雷达图数据")
    @GetMapping("/radar")
    public Result<Map<String, Object>> getRadarData() {
        return Result.success(analysisService.getRadarData());
    }
}
