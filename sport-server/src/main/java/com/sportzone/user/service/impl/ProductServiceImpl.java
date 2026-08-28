package com.sportzone.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportzone.common.dto.PageResult;
import com.sportzone.user.dto.CreateProductDTO;
import com.sportzone.user.dto.UpdateProductDTO;
import com.sportzone.user.entity.Order;
import com.sportzone.user.entity.OrderItem;
import com.sportzone.user.entity.Product;
import com.sportzone.user.entity.User;
import com.sportzone.user.mapper.OrderItemMapper;
import com.sportzone.user.mapper.OrderMapper;
import com.sportzone.user.mapper.ProductMapper;
import com.sportzone.user.mapper.UserMapper;
import com.sportzone.user.service.ProductService;
import com.sportzone.user.service.OssService;
import com.sportzone.user.vo.ProductReviewVO;
import com.sportzone.user.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final OssService ossService;

    public ProductServiceImpl(ProductMapper productMapper,
                              OrderItemMapper orderItemMapper,
                              OrderMapper orderMapper,
                              UserMapper userMapper,
                              OssService ossService) {
        this.productMapper = productMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.ossService = ossService;
    }

    @Override
    public void createProduct(CreateProductDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setSalesCount(0);
        product.setRatingScore(new java.math.BigDecimal("5.0"));
        product.setIsOn(dto.getIsOn() == null || dto.getIsOn() ? 1 : 0);
        product.setImages(emptyToNull(product.getImages()));
        product.setSpecs(emptyToNull(product.getSpecs()));
        product.setDetail(emptyToNull(product.getDetail()));
        syncProductFromSpecs(product);
        productMapper.insert(product);
    }

    @Override
    public void updateProduct(Long id, UpdateProductDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (dto.getCategoryId() != null) product.setCategoryId(dto.getCategoryId());
        if (StringUtils.hasText(dto.getName())) product.setName(dto.getName());
        if (dto.getSubtitle() != null) product.setSubtitle(dto.getSubtitle());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getOriginalPrice() != null) product.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getSalesCount() != null) product.setSalesCount(dto.getSalesCount());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getBadge() != null) product.setBadge(dto.getBadge());
        if (dto.getImages() != null) product.setImages(emptyToNull(dto.getImages()));
        if (dto.getSpecs() != null) {
            product.setSpecs(emptyToNull(dto.getSpecs()));
            syncProductFromSpecs(product);
        }
        if (dto.getDetail() != null) product.setDetail(emptyToNull(dto.getDetail()));
        if (dto.getIsOn() != null) product.setIsOn(Boolean.TRUE.equals(dto.getIsOn()) ? 1 : 0);

        productMapper.updateById(product);
    }

    @Override
    public void updateProduct(Long id, UpdateProductDTO dto, String oldImageUrl) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 处理图片OSS清理逻辑
        if (dto.getImage() != null && dto.getImage().isBlank()) {
            // 新图片为空字符串，删除旧图片
            if (oldImageUrl != null) {
                ossService.deleteFile(oldImageUrl);
            }
            dto.setImage("");
        } else if (dto.getImage() != null && !dto.getImage().equals(oldImageUrl)) {
            // 新图片与旧图片不同，删除旧图片
            if (oldImageUrl != null) {
                ossService.deleteFile(oldImageUrl);
            }
        }

        // 更新商品字段
        if (dto.getCategoryId() != null) product.setCategoryId(dto.getCategoryId());
        if (StringUtils.hasText(dto.getName())) product.setName(dto.getName());
        if (dto.getSubtitle() != null) product.setSubtitle(dto.getSubtitle());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getOriginalPrice() != null) product.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getSalesCount() != null) product.setSalesCount(dto.getSalesCount());
        if (dto.getImage() != null) product.setImage(dto.getImage());
        if (dto.getBadge() != null) product.setBadge(dto.getBadge());
        if (dto.getImages() != null) product.setImages(emptyToNull(dto.getImages()));
        if (dto.getSpecs() != null) {
            product.setSpecs(emptyToNull(dto.getSpecs()));
            syncProductFromSpecs(product);
        }
        if (dto.getDetail() != null) product.setDetail(emptyToNull(dto.getDetail()));
        if (dto.getIsOn() != null) product.setIsOn(Boolean.TRUE.equals(dto.getIsOn()) ? 1 : 0);

        productMapper.updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new RuntimeException("商品不存在");
        }
        productMapper.deleteById(id);
    }

    @Override
    public ProductVO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return toVO(product);
    }

    @Override
    public List<ProductVO> getProductList(Long categoryId, String sortBy, String sortDir) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsOn, 1);
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        applySort(wrapper, sortBy, sortDir);
        return productMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ProductVO> getProductPage(Long categoryId, String sortBy, String sortDir, long page, long size) {
        Page<Product> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        applySort(wrapper, sortBy, sortDir);
        Page<Product> result = productMapper.selectPage(mpPage, wrapper);

        PageResult<ProductVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setPages(result.getPages());
        pageResult.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public List<ProductVO> getHotProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsOn, 1);
        wrapper.orderByDesc(Product::getSalesCount);
        wrapper.last("LIMIT 4");
        return productMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> searchProducts(String keyword, Long categoryId, String sortBy, String sortDir) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getIsOn, 1);
        wrapper.and(w -> w.like(Product::getName, keyword)
                .or()
                .like(Product::getSubtitle, keyword));
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        applySort(wrapper, sortBy, sortDir);
        return productMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private void applySort(LambdaQueryWrapper<Product> wrapper, String sortBy, String sortDir) {
        boolean asc = "asc".equalsIgnoreCase(sortDir);
        if ("price".equals(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getPrice);
            else wrapper.orderByDesc(Product::getPrice);
        } else if ("salesCount".equals(sortBy) || "sales".equals(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getSalesCount);
            else wrapper.orderByDesc(Product::getSalesCount);
        } else {
            wrapper.orderByDesc(Product::getSalesCount);
        }
    }

    private String emptyToNull(String value) {
        return (value != null && value.trim().isEmpty()) ? null : value;
    }

    /**
     * 根据 specs 中的 specItems 同步商品的 stock 和 price。
     * - stock = 所有 specItem.stock 之和
     * - price = 所有 specItem.price 的最小值
     * 仅当 specs 包含有效 specItems 时才同步。
     */
    private void syncProductFromSpecs(Product product) {
        String specsJson = product.getSpecs();
        if (specsJson == null || specsJson.isBlank()) return;

        List<Map<String, Object>> specItems = parseSpecItems(specsJson);
        if (specItems.isEmpty()) return;

        // 计算总库存
        int totalStock = specItems.stream()
                .map(item -> item.get("stock"))
                .filter(Objects::nonNull)
                .mapToInt(s -> ((Number) s).intValue())
                .sum();
        product.setStock(totalStock);

        // 取最低价
        BigDecimal minPrice = specItems.stream()
                .map(item -> item.get("price"))
                .filter(Objects::nonNull)
                .map(p -> new BigDecimal(p.toString()))
                .min(BigDecimal::compareTo)
                .orElse(product.getPrice());
        product.setPrice(minPrice);
    }

    private ProductVO toVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    @Override
    public List<ProductReviewVO> getProductReviews(Long productId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
                        .isNotNull(OrderItem::getRating)
                        .orderByDesc(OrderItem::getReviewTime));

        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> orderIds = items.stream()
                .map(OrderItem::getOrderId)
                .distinct()
                .collect(Collectors.toList());

        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        Map<Long, Long> orderUserMap = orders.stream()
                .collect(Collectors.toMap(Order::getId, Order::getUserId));

        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return items.stream().map(item -> {
            ProductReviewVO vo = new ProductReviewVO();
            vo.setId(item.getId());
            vo.setRating(item.getRating());
            vo.setReviewContent(item.getReviewContent());
            vo.setReviewImages(item.getReviewImages());
            vo.setReviewTime(item.getReviewTime());
            vo.setReplyContent(item.getReplyContent());
            vo.setReplyTime(item.getReplyTime());

            Long userId = orderUserMap.get(item.getOrderId());
            if (userId != null) {
                vo.setUserId(userId);
                User user = userMap.get(userId);
                if (user != null) {
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                    vo.setUserLevel(user.getUserLevel());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 specs JSON，兼容新旧格式。
     * 旧格式：[{"name":"颜色","options":["黑色","白色"]}]  → 返回空列表
     * 新格式：{"specGroups":[...],"specItems":[...]}        → 返回 specItems
     */
    public static List<Map<String, Object>> parseSpecItems(String specsJson) {
        if (specsJson == null || specsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            Object parsed = objectMapper.readValue(specsJson, Object.class);
            if (parsed instanceof List) {
                return Collections.emptyList();
            }
            if (parsed instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) parsed;
                Object specItems = map.get("specItems");
                if (specItems instanceof List) {
                    return objectMapper.convertValue(specItems,
                            new TypeReference<List<Map<String, Object>>>() {});
                }
            }
        } catch (Exception e) {
            // JSON 解析失败，返回空列表
        }
        return Collections.emptyList();
    }

    /**
     * 根据用户选择的规格字符串，查找对应的 specItem。
     * @param specsJson  商品的 specs JSON 字符串
     * @param selectedSpec  用户选中的规格，如 "黑色 / 64GB"
     * @return 匹配的 specItem Map（含 price, originalPrice, stock 等），未找到返回 null
     */
    public static Map<String, Object> findSpecItem(String specsJson, String selectedSpec) {
        if (selectedSpec == null || selectedSpec.trim().isEmpty()) {
            return null;
        }
        List<Map<String, Object>> items = parseSpecItems(specsJson);
        for (Map<String, Object> item : items) {
            Object specs = item.get("specs");
            if (selectedSpec.equals(specs)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取规格对应的价格，未找到时返回商品基础价格。
     */
    public static BigDecimal getSpecPrice(String specsJson, String selectedSpec, BigDecimal basePrice) {
        Map<String, Object> item = findSpecItem(specsJson, selectedSpec);
        if (item != null && item.get("price") != null) {
            return new BigDecimal(item.get("price").toString());
        }
        return basePrice;
    }

    /**
     * 获取规格对应的原价，未找到时返回商品基础原价。
     */
    public static BigDecimal getSpecOriginalPrice(String specsJson, String selectedSpec, BigDecimal baseOriginalPrice) {
        Map<String, Object> item = findSpecItem(specsJson, selectedSpec);
        if (item != null && item.get("originalPrice") != null) {
            return new BigDecimal(item.get("originalPrice").toString());
        }
        return baseOriginalPrice;
    }

    /**
     * 获取规格对应的库存，未找到时返回商品总库存。
     */
    public static Integer getSpecStock(String specsJson, String selectedSpec, Integer baseStock) {
        Map<String, Object> item = findSpecItem(specsJson, selectedSpec);
        if (item != null && item.get("stock") != null) {
            return ((Number) item.get("stock")).intValue();
        }
        return baseStock;
    }
}