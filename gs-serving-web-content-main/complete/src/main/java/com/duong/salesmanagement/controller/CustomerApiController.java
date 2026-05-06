package com.duong.salesmanagement.controller;

import com.duong.salesmanagement.model.MenuItem;
import com.duong.salesmanagement.model.RestaurantProfile;
import com.duong.salesmanagement.repository.MenuItemRepository;
import com.duong.salesmanagement.repository.RestaurantProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@SuppressWarnings("null")
public class CustomerApiController {

    private final RestaurantProfileRepository restaurantProfileRepository;
    private final MenuItemRepository menuItemRepository;

    public CustomerApiController(RestaurantProfileRepository restaurantProfileRepository,
                                 MenuItemRepository menuItemRepository) {
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // 1. Get list of restaurants
    @GetMapping("/restaurants")
    public ResponseEntity<?> getRestaurants() {
        List<RestaurantProfile> restaurants = restaurantProfileRepository.findAll();
        List<RestaurantDTO> dtos = restaurants.stream().map(r -> new RestaurantDTO(
                r.getId(),
                r.getRestaurantName(),
                r.getAddress(),
                r.getAverageRating(),
                r.isOpen()
        )).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // 2. Get restaurant details + menu items
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<?> getRestaurantDetail(@PathVariable Long id) {
        RestaurantProfile restaurant = restaurantProfileRepository.findById(id).orElse(null);
        if (restaurant == null) {
            return ResponseEntity.notFound().build();
        }

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantAndIsAvailableTrue(restaurant);
        List<MenuItemDTO> menuItemDTOs = menuItems.stream().map(m -> new MenuItemDTO(
                m.getId(),
                m.getName(),
                m.getDescription(),
                m.getPrice(),
                m.getImageUrl()
        )).collect(Collectors.toList());

        RestaurantDetailDTO detailDTO = new RestaurantDetailDTO(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                restaurant.getAddress(),
                restaurant.getAverageRating(),
                restaurant.isOpen(),
                menuItemDTOs
        );

        return ResponseEntity.ok(detailDTO);
    }

    // DTOs
    public static class RestaurantDTO {
        public Long id;
        public String name;
        public String address;
        public Double rating;
        public boolean isOpen;

        public RestaurantDTO(Long id, String name, String address, Double rating, boolean isOpen) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.rating = rating;
            this.isOpen = isOpen;
        }
    }

    public static class MenuItemDTO {
        public Long id;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;

        public MenuItemDTO(Long id, String name, String description, Double price, String imageUrl) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.imageUrl = imageUrl;
        }
    }

    public static class RestaurantDetailDTO {
        public Long id;
        public String name;
        public String address;
        public Double rating;
        public boolean isOpen;
        public List<MenuItemDTO> menuItems;

        public RestaurantDetailDTO(Long id, String name, String address, Double rating, boolean isOpen, List<MenuItemDTO> menuItems) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.rating = rating;
            this.isOpen = isOpen;
            this.menuItems = menuItems;
        }
    }
}
