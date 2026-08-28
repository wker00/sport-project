package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportzone.admin.dto.CreateCategoryDTO;
import com.sportzone.admin.dto.UpdateCategoryDTO;
import com.sportzone.user.entity.Category;
import com.sportzone.user.mapper.CategoryMapper;
import com.sportzone.user.service.CategoryService;
import com.sportzone.user.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryVO> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getTopCategories(Integer limit) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        if (limit != null) {
            wrapper.last("LIMIT " + limit);
        }
        return categoryMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        return toVO(category);
    }

    @Override
    public void createCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, UpdateCategoryDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        if (StringUtils.hasText(dto.getName())) category.setName(dto.getName());
        if (dto.getImage() != null) category.setImage(dto.getImage());
        if (dto.getIcon() != null) category.setIcon(dto.getIcon());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) category.setSortOrder(dto.getSortOrder());
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new RuntimeException("分类不存在");
        }
        categoryMapper.deleteById(id);
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
