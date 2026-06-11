package com.trial.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trial.server.config.SecurityUtil;
import com.trial.server.entity.Category;
import com.trial.server.entity.Tag;
import com.trial.server.mapper.CategoryMapper;
import com.trial.server.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类与标签服务
 */
@Service
@RequiredArgsConstructor
public class CategoryTagService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    // ===== 分类管理 =====

    public List<Category> listCategories() {
        Long userId = SecurityUtil.getCurrentUserId();
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getUserId, userId)
                        .orderByAsc(Category::getSort)
        );
    }

    public void addCategory(Category category) {
        category.setUserId(SecurityUtil.getCurrentUserId());
        categoryMapper.insert(category);
    }

    public void updateCategory(Category category) {
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // ===== 标签管理 =====

    public List<Tag> listTags() {
        Long userId = SecurityUtil.getCurrentUserId();
        return tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getUserId, userId)
                        .orderByDesc(Tag::getCreateTime)
        );
    }

    public void addTag(Tag tag) {
        tag.setUserId(SecurityUtil.getCurrentUserId());
        tagMapper.insert(tag);
    }

    public void updateTag(Tag tag) {
        tagMapper.updateById(tag);
    }

    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
    }
}
