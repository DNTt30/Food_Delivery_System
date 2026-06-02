package com.duong.salesmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    // Thư mục lưu trữ ảnh trên server thật
    // Lưu vào thư mục 'uploads' ngang hàng với file .jar để dữ liệu không bị lỗi path
    private final String UPLOAD_DIR = "uploads/";

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn một file để tải lên."));
        }

        try {
            // 1. Tạo thư mục nếu chưa tồn tại
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Tạo tên file duy nhất để tránh trùng lặp
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 3. Đường dẫn lưu file
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            // 4. Copy file vào thư mục đích
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Trả về URL của ảnh (đường dẫn tương đối cho web)
            String imageUrl = "/images/" + fileName;
            
            return ResponseEntity.ok(Map.of(
                "message", "Tải ảnh lên thành công!",
                "url", imageUrl
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi lưu file: " + e.getMessage()));
        }
    }
}
