package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.Category;
import com.duong.salesmanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryClassifierService {

    private final CategoryRepository categoryRepository;

    public CategoryClassifierService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category classify(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return getDefaultCategory();
        }

        String name = itemName.toLowerCase().trim();

        // 1. Đồ uống (Drinks) keywords
        if (name.contains("trà") || name.contains("café") || name.contains("cà phê") ||
            name.contains("nước") || name.contains("sinh tố") || name.contains("sữa") ||
            name.contains("pepsi") || name.contains("coca") || name.contains("fanta") ||
            name.contains("sprite") || name.contains("juice") || name.contains("tea") ||
            name.contains("coffee") || name.contains("drink") || name.contains("soda") ||
            name.contains("chanh")) {
            return getOrCreateCategory("Đồ uống", "Trà, cà phê, nước giải khát các loại");
        }

        // 2. Đồ ăn nhanh (Fast Food / Snacks) keywords
        if (name.contains("bánh mì") || name.contains("bánh mỳ") || name.contains("burger") ||
            name.contains("pizza") || name.contains("gà rán") || name.contains("croissant") ||
            name.contains("sandwich") || name.contains("khoai tây chiên") || name.contains("snack") ||
            name.contains("ăn vặt") || name.contains("nem chua") || name.contains("bánh ngọt") ||
            name.contains("bánh croissant")) {
            return getOrCreateCategory("Đồ ăn nhanh", "Bánh mì, burger, pizza và thức ăn nhanh");
        }

        // 3. Đồ ăn (Main Food) - Default
        return getOrCreateCategory("Đồ ăn", "Cơm, bún, phở, mì và các món ăn chính");
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setName(name);
                    cat.setDescription(description);
                    return categoryRepository.save(cat);
                });
    }

    private Category getDefaultCategory() {
        return getOrCreateCategory("Đồ ăn", "Cơm, bún, phở, mì và các món ăn chính");
    }
}
