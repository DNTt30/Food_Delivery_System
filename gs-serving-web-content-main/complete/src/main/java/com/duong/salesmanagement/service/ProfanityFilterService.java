package com.duong.salesmanagement.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service lọc từ ngữ thô tục (Profanity Filter)
 * Bảo vệ môi trường kinh doanh lành mạnh
 */
@Service
public class ProfanityFilterService {
    
    // Danh sách từ ngữ thô tục cấm (có thể mở rộng)
    private static final Set<String> BANNED_WORDS = new HashSet<>(Arrays.asList(
        // Từ ngữ không lành mạnh
        "đ*", "địt", "chó", "mẹ kiếp", "con chó", "đụ", "lồn",
        "ba chín", "óc vịt", "ngu ngốc", "khốn", "tệ hại",
        // Các biến thể
        "d***", "f***", "b****"
    ));
    
    private static final String CENSORED_WORD = "***";
    
    /**
     * Lọc từ ngữ thô tục trong text
     * @param text Văn bản cần lọc
     * @return Văn bản đã lọc
     */
    public String filterProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String filtered = text;
        
        for (String word : BANNED_WORDS) {
            // Tạo regex pattern (case-insensitive, word boundary)
            String pattern = "(?i)\\b" + Pattern.quote(word) + "\\b";
            filtered = filtered.replaceAll(pattern, CENSORED_WORD);
        }
        
        return filtered;
    }
    
    /**
     * Kiểm tra xem text có chứa từ ngữ thô tục không
     * @param text Văn bản cần kiểm tra
     * @return true nếu có từ ngữ thô tục, false nếu không
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        for (String word : BANNED_WORDS) {
            String pattern = "(?i)\\b" + Pattern.quote(word) + "\\b";
            if (Pattern.compile(pattern).matcher(lowerText).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Lấy số lượng từ ngữ thô tục trong text
     */
    public int countProfanityWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        String lowerText = text.toLowerCase();
        
        for (String word : BANNED_WORDS) {
            String pattern = "(?i)\\b" + Pattern.quote(word) + "\\b";
            Pattern p = Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(lowerText);
            while (m.find()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Thêm từ ngữ vào danh sách cấm
     */
    public void addBannedWord(String word) {
        if (word != null && !word.isEmpty()) {
            BANNED_WORDS.add(word.toLowerCase());
        }
    }
    
    /**
     * Xóa từ ngữ khỏi danh sách cấm
     */
    public void removeBannedWord(String word) {
        if (word != null) {
            BANNED_WORDS.remove(word.toLowerCase());
        }
    }
}
