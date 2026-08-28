package com.sportzone.user.service;

import com.sportzone.admin.dto.CreateCategoryDTO;
import com.sportzone.admin.dto.UpdateCategoryDTO;
import com.sportzone.user.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> getCategoryList();

    List<CategoryVO> getTopCategories(Integer limit);

    CategoryVO getCategoryById(Long id);

    void createCategory(CreateCategoryDTO dto);

    void updateCategory(Long id, UpdateCategoryDTO dto);

    void deleteCategory(Long id);
}
