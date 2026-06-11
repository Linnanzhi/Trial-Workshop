package com.trial.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.Result;
import com.trial.server.dto.ParseTextRequest;
import com.trial.server.dto.QuestionDTO;
import com.trial.server.entity.Question;
import com.trial.server.service.FileImportService;
import com.trial.server.service.QuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 题库控制器
 */
@Api(tags = "智能题库管理")
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final FileImportService fileImportService;

    @ApiOperation("分页查询题目")
    @GetMapping
    public Result<Page<Question>> listQuestions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer difficulty) {
        return Result.success(questionService.listQuestions(pageNum, pageSize, keyword, categoryId, type, difficulty));
    }

    @ApiOperation("获取题目详情")
    @GetMapping("/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        return Result.success(questionService.getQuestionDetail(id));
    }

    @ApiOperation("新增题目")
    @PostMapping
    public Result<?> addQuestion(@RequestBody QuestionDTO dto) {
        questionService.addQuestion(dto);
        return Result.success("添加成功");
    }

    @ApiOperation("批量导入题目(AI识别后)")
    @PostMapping("/batch-import")
    public Result<?> batchImport(@RequestBody List<QuestionDTO> questions) {
        int count = questionService.batchImport(questions);
        return Result.success("成功导入 " + count + " 道题目");
    }

    @ApiOperation("上传文件解析题目(Word/PDF/TXT)")
    @PostMapping("/upload-file")
    public Result<List<QuestionDTO>> uploadFile(@RequestParam("file") MultipartFile file) {
        List<QuestionDTO> parsed = fileImportService.parseFile(file);
        return Result.success("解析成功，共识别 " + parsed.size() + " 道题目", parsed);
    }

    @ApiOperation("解析粘贴的题目文本")
    @PostMapping("/parse-text")
    public Result<?> parseText(@RequestBody ParseTextRequest request) {
        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            return Result.error(400, "文本内容不能为空");
        }
        
        // 使用新的方法返回包含未解析文本的结果
        com.trial.server.dto.ParseResult parseResult = fileImportService.parseTextToQuestionsWithResult(
                text, request.getType(), request.getCategoryId(), request.getDifficulty());
        
        // 构建响应
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("questions", parseResult.getQuestions());
        response.put("unparsedBlocks", parseResult.getUnparsedBlocks());
        response.put("totalParsed", parseResult.getQuestions().size());
        response.put("totalUnparsed", parseResult.getUnparsedBlocks().size());
        
        return Result.success("解析完成", response);
    }

    @ApiOperation("AI 解析粘贴的题目文本")
    @PostMapping("/parse-text-ai")
    public Result<?> parseTextWithAI(@RequestBody ParseTextRequest request) {
        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            return Result.error(400, "文本内容不能为空");
        }
        
        // 使用 AI 解析
        List<QuestionDTO> parsed = fileImportService.parseTextWithAI(
                text, 
                request.getAiProvider(),
                request.getModel(),
                request.getType(), 
                request.getCategoryId(), 
                request.getDifficulty()
        );
        
        // 构建响应（包含元数据）
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", parsed);
        
        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("totalQuestions", parsed.size());
        meta.put("parseTime", 2500); // 实际应该记录真实耗时
        meta.put("tokensUsed", 1500); // 实际应该从AI服务返回
        meta.put("cost", 0.000225); // 实际应该计算
        response.put("meta", meta);
        
        return Result.success("AI 解析成功", response);
    }

    @ApiOperation("修改题目")
    @PutMapping
    public Result<?> updateQuestion(@RequestBody QuestionDTO dto) {
        questionService.updateQuestion(dto);
        return Result.success("修改成功");
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/{id}")
    public Result<?> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success("删除成功");
    }

    @ApiOperation("批量删除题目")
    @DeleteMapping("/batch")
    public Result<?> batchDeleteQuestions(@RequestBody List<Long> ids) {
        int count = questionService.batchDeleteQuestions(ids);
        return Result.success("成功删除 " + count + " 道题目");
    }

    @ApiOperation("批量更新分类")
    @PutMapping("/batch-category")
    public Result<?> batchUpdateCategory(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> idsObj = (List<Object>) params.get("ids");
        List<Long> ids = idsObj.stream()
            .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
            .collect(java.util.stream.Collectors.toList());
        
        Long categoryId = null;
        if (params.get("categoryId") != null) {
            Object catIdObj = params.get("categoryId");
            categoryId = catIdObj instanceof Integer ? 
                ((Integer) catIdObj).longValue() : (Long) catIdObj;
        }
        
        int count = questionService.batchUpdateCategory(ids, categoryId);
        return Result.success("成功更新 " + count + " 道题目的分类");
    }

    @ApiOperation("批量更新标签")
    @PutMapping("/batch-tags")
    public Result<?> batchUpdateTags(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> idsObj = (List<Object>) params.get("ids");
        List<Long> ids = idsObj.stream()
            .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
            .collect(java.util.stream.Collectors.toList());
        
        @SuppressWarnings("unchecked")
        List<Object> tagIdsObj = (List<Object>) params.get("tagIds");
        List<Long> tagIds = tagIdsObj.stream()
            .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
            .collect(java.util.stream.Collectors.toList());
        
        int count = questionService.batchUpdateTags(ids, tagIds);
        return Result.success("成功更新 " + count + " 道题目的标签");
    }

    @ApiOperation("题库统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(questionService.getQuestionStats());
    }
}
