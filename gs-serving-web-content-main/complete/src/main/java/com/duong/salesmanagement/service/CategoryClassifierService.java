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

        // 1. Trà sữa (phải kiểm tra trước "trà" để không bị lẫn vào Đồ uống)
        if (name.contains("trà sữa") || name.contains("tra sua") ||
            name.contains("milk tea") || name.contains("milktea")) {
            return getOrCreateCategory("Trà sữa", "Trà sữa các loại topping");
        }

        // 2. Trà chanh
        if (name.contains("trà chanh") || name.contains("lemon tea") ||
            name.contains("chanh sả") || name.contains("trà đào") ||
            name.contains("trà gừng") || name.contains("trà bạc hà")) {
            return getOrCreateCategory("Trà chanh", "Trà chanh, trà đào và trà lạnh");
        }

        // 3. Đồ uống chung (cà phê, nước, sinh tố...)
        if (name.contains("cà phê") || name.contains("cafe") || name.contains("coffee") ||
            name.contains("café") || name.contains("sinh tố") || name.contains("nước ép") ||
            name.contains("nước dừa") || name.contains("pepsi") || name.contains("coca") ||
            name.contains("fanta") || name.contains("sprite") || name.contains("soda") ||
            name.contains("juice") || name.contains("drink") || name.contains("nước chanh") ||
            name.contains("nước mía") || name.contains("smoothie") || name.contains("beer") ||
            name.contains("bia") || name.contains("nước ngọt")) {
            return getOrCreateCategory("Đồ uống", "Cà phê, nước giải khát, sinh tố các loại");
        }

        // 4. Trà nói chung (trà oolong, trà xanh,... không phải trà sữa/trà chanh)
        if (name.contains("trà") || name.contains("tea")) {
            return getOrCreateCategory("Trà chanh", "Trà chanh, trà đào và trà lạnh");
        }

        // 5. Cơm
        if (name.contains("cơm") || name.contains("rice") ||
            name.contains("com tam") || name.contains("cơm tấm") ||
            name.contains("cơm chiên") || name.contains("cơm rang") ||
            name.contains("cơm gà") || name.contains("cơm sườn") ||
            name.contains("cơm bò") || name.contains("cơm hải sản")) {
            return getOrCreateCategory("Cơm", "Cơm tấm, cơm gà, cơm chiên và các món cơm");
        }

        // 6. Ăn vặt
        if (name.contains("ăn vặt") || name.contains("snack") || name.contains("kẹo") ||
            name.contains("bánh tráng") || name.contains("nem chua") ||
            name.contains("ô mai") || name.contains("hạt") ||
            name.contains("popcorn") || name.contains("bắp rang") ||
            name.contains("xúc xích") || name.contains("đậu") || name.contains("bò khô")) {
            return getOrCreateCategory("Ăn vặt", "Đồ ăn vặt, snack, hạt và các loại bánh kẹo");
        }

        // 7. Đồ ăn nhanh (burger, pizza, gà rán...)
        if (name.contains("burger") || name.contains("pizza") ||
            name.contains("sandwich") || name.contains("hotdog") ||
            name.contains("hot dog") || name.contains("gà rán") ||
            name.contains("khoai tây chiên") || name.contains("khoai chiên") ||
            name.contains("croissant") || name.contains("bánh mì") ||
            name.contains("bánh mỳ") || name.contains("wrap")) {
            return getOrCreateCategory("Đồ ăn nhanh", "Burger, pizza, gà rán và thức ăn nhanh");
        }

        // 8. Bánh ngọt / Tráng miệng
        if (name.contains("bánh ngọt") || name.contains("cake") || name.contains("bánh kem") ||
            name.contains("bánh bông lan") || name.contains("macaron") || name.contains("tiramisu") ||
            name.contains("tráng miệng") || name.contains("dessert") || name.contains("kem") ||
            name.contains("chè") || name.contains("pudding") || name.contains("waffle") ||
            name.contains("donut") || name.contains("bánh cupcake")) {
            return getOrCreateCategory("Bánh ngọt", "Bánh kem, tráng miệng, chè và các loại bánh ngọt");
        }

        // 9. Phở / Bún / Mì
        if (name.contains("phở") || name.contains("bún") || name.contains("hủ tiếu") ||
            name.contains("mì") || name.contains("noodle") || name.contains("ramen") ||
            name.contains("udon") || name.contains("bún bò") || name.contains("bún riêu") ||
            name.contains("bún chả") || name.contains("mì quảng")) {
            return getOrCreateCategory("Phở & Bún", "Phở, bún bò, bún riêu và các món nước");
        }

        // 10. Đồ ăn chung (default)
        return getOrCreateCategory("Đồ ăn", "Các món ăn chính đa dạng");
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
        return getOrCreateCategory("Đồ ăn", "Các món ăn chính đa dạng");
    }
}
