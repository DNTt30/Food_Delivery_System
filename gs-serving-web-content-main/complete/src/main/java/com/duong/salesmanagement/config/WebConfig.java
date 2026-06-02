package com.duong.salesmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Ánh xạ URL /images/** để đọc ảnh từ 2 nguồn:
        // 1. Thư mục 'uploads' bên ngoài file .jar (cho ảnh mới upload)
        // 2. Thư mục 'classpath:/static/images/' bên trong file .jar (cho ảnh mặc định có sẵn)
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + uploadPath + "/", "classpath:/static/images/");
    }
}
