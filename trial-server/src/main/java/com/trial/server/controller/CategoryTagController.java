package com.trial.server.controller;

import com.trial.server.common.Result;
import com.trial.server.entity.Category;
import com.trial.server.entity.Tag;
import com.trial.server.service.CategoryTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类与标签控制器
 */
@Api(tags = "分类与标签管理")
@RestController
@RequestMapping("/category-tag")
@RequiredArgsConstructor
public class CategoryTagController {

    private final CategoryTagService categoryTagService;

    // ===== 分类 =====

    @ApiOperation("获取分类列表")
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return Result.success(categoryTagService.listCategories());
    }

    @ApiOperation("新增分类")
    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        categoryTagService.addCategory(category);
        return Result.success("添加成功");
    }

    @ApiOperation("修改分类")
    @PutMapping("/categories")
    public Result<?> updateCategory(@RequestBody Category category) {
        categoryTagService.updateCategory(category);
        return Result.success("修改成功");
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryTagService.deleteCategory(id);
        return Result.success("删除成功");
    }

    // ===== 标签 =====

    @ApiOperation("获取标签列表")
    @GetMapping("/tags")
    public Result<List<Tag>> listTags() {
        return Result.success(categoryTagService.listTags());
    }

    @ApiOperation("新增标签")
    @PostMapping("/tags")
    public Result<?> addTag(@RequestBody Tag tag) {
        categoryTagService.addTag(tag);
        return Result.success("添加成功");
    }

    @ApiOperation("修改标签")
    @PutMapping("/tags")
    public Result<?> updateTag(@RequestBody Tag tag) {
        categoryTagService.updateTag(tag);
        return Result.success("修改成功");
    }

    @ApiOperation("删除标签")
    @DeleteMapping("/tags/{id}")
    public Result<?> deleteTag(@PathVariable Long id) {
        categoryTagService.deleteTag(id);
        return Result.success("删除成功");
    }
}
