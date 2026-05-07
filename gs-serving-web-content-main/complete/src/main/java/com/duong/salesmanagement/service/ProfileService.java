package com.duong.salesmanagement.service;

import com.duong.salesmanagement.dto.PasswordChangeDTO;
import com.duong.salesmanagement.dto.ProfileDTO;
import com.duong.salesmanagement.model.*;
import com.duong.salesmanagement.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class ProfileService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RestaurantProfileRepository restaurantProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository,
                          CustomerProfileRepository customerProfileRepository,
                          DriverProfileRepository driverProfileRepository,
                          RestaurantProfileRepository restaurantProfileRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.restaurantProfileRepository = restaurantProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ProfileDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProfileDTO dto = new ProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());

        if (user.getRole() == Role.CUSTOMER) {
            CustomerProfile profile = customerProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        CustomerProfile newProfile = new CustomerProfile();
                        newProfile.setUser(user);
                        return customerProfileRepository.save(newProfile);
                    });
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setDeliveryAddress(profile.getDeliveryAddress());
        } else if (user.getRole() == Role.DRIVER) {
            DriverProfile profile = driverProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        DriverProfile newProfile = new DriverProfile();
                        newProfile.setUser(user);
                        return driverProfileRepository.save(newProfile);
                    });
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setLicensePlate(profile.getLicensePlate());
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfile profile = restaurantProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        RestaurantProfile newProfile = new RestaurantProfile();
                        newProfile.setUser(user);
                        return restaurantProfileRepository.save(newProfile);
                    });
            dto.setRestaurantName(profile.getRestaurantName());
            dto.setAddress(profile.getAddress());
            dto.setBannerUrl(profile.getBannerUrl());
            // optionally we could include isOpen, but we omitted from ProfileDTO for now
        }

        return dto;
    }

    @Transactional
    public void updateProfile(String username, ProfileDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        userRepository.save(user);

        if (user.getRole() == Role.CUSTOMER) {
            CustomerProfile profile = customerProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        CustomerProfile p = new CustomerProfile();
                        p.setUser(user);
                        return p;
                    });
            if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getDeliveryAddress() != null) profile.setDeliveryAddress(dto.getDeliveryAddress());
            customerProfileRepository.save(profile);
        } else if (user.getRole() == Role.DRIVER) {
            DriverProfile profile = driverProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        DriverProfile p = new DriverProfile();
                        p.setUser(user);
                        return p;
                    });
            if (dto.getPhoneNumber() != null) profile.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getLicensePlate() != null) profile.setLicensePlate(dto.getLicensePlate());
            driverProfileRepository.save(profile);
        } else if (user.getRole() == Role.RESTAURANT) {
            RestaurantProfile profile = restaurantProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        RestaurantProfile p = new RestaurantProfile();
                        p.setUser(user);
                        return p;
                    });
            if (dto.getRestaurantName() != null) profile.setRestaurantName(dto.getRestaurantName());
            if (dto.getAddress() != null) profile.setAddress(dto.getAddress());
            if (dto.getBannerUrl() != null) profile.setBannerUrl(dto.getBannerUrl());
            restaurantProfileRepository.save(profile);
        }
    }

    @Transactional
    public void changePassword(String username, PasswordChangeDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}
