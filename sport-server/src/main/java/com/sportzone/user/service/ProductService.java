package com.sportzone.user.service;

import com.sportzone.common.dto.PageResult;
import com.sportzone.user.dto.CreateProductDTO;
import com.sportzone.user.dto.UpdateProductDTO;
import com.sportzone.user.vo.ProductReviewVO;
import com.sportzone.user.vo.ProductVO;

import java.util.List;

public interface ProductService {

    void createProduct(CreateProductDTO dto);

    void updateProduct(Long id, UpdateProductDTO dto);

    void updateProduct(Long id, UpdateProductDTO dto, String oldImageUrl);

    void deleteProduct(Long id);

    ProductVO getProductById(Long id);

    List<ProductVO> getProductList(Long categoryId, String sortBy, String sortDir);

    PageResult<ProductVO> getProductPage(Long categoryId, String sortBy, String sortDir, long page, long size);

    List<ProductVO> getHotProducts();

    List<ProductVO> searchProducts(String keyword, Long categoryId, String sortBy, String sortDir);

    List<ProductReviewVO> getProductReviews(Long productId);

}