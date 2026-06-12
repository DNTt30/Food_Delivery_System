package com.duong.salesmanagement.service;

import com.duong.salesmanagement.model.FoodOrder;
import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.OrderItem;
import com.duong.salesmanagement.model.OrderStatus;
import com.duong.salesmanagement.model.Category;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.FoodOrderRepository;
import com.duong.salesmanagement.repository.CategoryRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataMigrationService {

    private final FoodOrderRepository foodOrderRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryClassifierService categoryClassifierService;

    public DataMigrationService(FoodOrderRepository foodOrderRepository, 
                                MenuItemRepository menuItemRepository,
                                CategoryRepository categoryRepository,
                                CategoryClassifierService categoryClassifierService) {
        this.foodOrderRepository = foodOrderRepository;
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.categoryClassifierService = categoryClassifierService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfillSoldCount() {
        // Fetch all completed orders
        List<FoodOrder> completedOrders = foodOrderRepository.findByStatus(OrderStatus.COMPLETED);
        
        // Sum quantities per menu item
        Map<MenuItem, Integer> salesCountMap = completedOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getMenuItem, 
                         Collectors.summingInt(OrderItem::getQuantity)));

        // Update menu items
        for (Map.Entry<MenuItem, Integer> entry : salesCountMap.entrySet()) {
            MenuItem item = entry.getKey();
            Integer newCount = entry.getValue();
            if (item.getSoldCount() == null || item.getSoldCount() < newCount) {
                item.setSoldCount(newCount);
                menuItemRepository.save(item);
            }
        }
        System.out.println("DataMigrationService: Backfilled sold count for " + salesCountMap.size() + " menu items.");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfillCategories() {
        // Find all menu items
        List<MenuItem> allItems = menuItemRepository.findAll();
        for (MenuItem item : allItems) {
            Category classified = categoryClassifierService.classify(item.getName());
            item.setCategory(classified);
            menuItemRepository.save(item);
        }
        
        // Clean up unused categories
        List<Category> allCategories = categoryRepository.findAll();
        for (Category cat : allCategories) {
            boolean hasReference = menuItemRepository.findAll().stream()
                    .anyMatch(m -> m.getCategory() != null && m.getCategory().getId().equals(cat.getId()));
            if (!hasReference) {
                categoryRepository.delete(cat);
            }
        }
        System.out.println("DataMigrationService: Reclassified all menu items and cleaned up unused categories.");
    }
}
