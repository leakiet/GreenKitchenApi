package com.greenkitchen.portal.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greenkitchen.portal.dtos.AIContentRequest;
import com.greenkitchen.portal.dtos.AIContentResponse;
import com.greenkitchen.portal.services.AIContentService;

@Service
public class AIContentServiceImpl implements AIContentService {

    private static final Logger log = LoggerFactory.getLogger(AIContentServiceImpl.class);

    @Autowired
    private ChatClient chatClient;

    @Override
    public AIContentResponse generatePostContent(AIContentRequest request) {
        try {
            log.info("🤖 Generating complete post content for topic: {}", request.getTopic());
            
            // Build prompt manually to avoid template parsing issues
            String prompt = buildPromptForCompleteContent(request);
            
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            String content = response.getResult().getOutput().getText();
            log.info("✅ AI generated content successfully, length: {} characters", content.length());
            
            return parseAIResponse(content);
            
        } catch (Exception e) {
            log.error("❌ Error generating post content: {}", e.getMessage(), e);
            return createErrorResponse("Lỗi khi tạo nội dung: " + e.getMessage());
        }
    }

    @Override
    public AIContentResponse generateTitleOnly(AIContentRequest request) {
        try {
            log.info("🤖 Generating title only for topic: {}", request.getTopic());
            
            String prompt = String.format(
                "Hãy tạo một tiêu đề hấp dẫn và SEO-friendly cho bài viết về chủ đề '%s' " +
                "trong danh mục '%s' với phong cách '%s' cho đối tượng '%s'. " +
                "Tiêu đề phải thu hút sự chú ý và phù hợp với thương hiệu Green Kitchen - nhà hàng chuyên về thực phẩm sạch, an toàn và giàu dinh dưỡng. " +
                "Chỉ trả về tiêu đề, không có gì khác.",
                request.getTopic() != null ? request.getTopic() : "",
                request.getCategory() != null ? request.getCategory() : "",
                request.getStyle() != null ? request.getStyle() : "friendly",
                request.getTargetAudience() != null ? request.getTargetAudience() : "customers"
            );
            
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String title = response.getResult().getOutput().getText().trim();
            
            AIContentResponse result = new AIContentResponse();
            result.setTitle(title);
            result.setSlug(generateSlug(title));
            result.setStatus("success");
            
            log.info("✅ AI generated title successfully: {}", title);
            return result;
            
        } catch (Exception e) {
            log.error("❌ Error generating title: {}", e.getMessage(), e);
            return createErrorResponse("Lỗi khi tạo tiêu đề: " + e.getMessage());
        }
    }

    @Override
    public AIContentResponse generateContentOnly(AIContentRequest request) {
        try {
            log.info("🤖 Generating content only for topic: {}", request.getTopic());
            
            String prompt = String.format(
                "Hãy viết nội dung bài viết về chủ đề '%s' trong danh mục '%s' " +
                "với phong cách '%s' cho đối tượng '%s'. " +
                "Bài viết khoảng %d từ và viết bằng tiếng %s. " +
                "Nội dung phải chất lượng cao, hữu ích và phù hợp với thương hiệu Green Kitchen. " +
                "%s " +
                "Hãy trả về nội dung được cấu trúc rõ ràng với các đoạn văn.",
                request.getTopic() != null ? request.getTopic() : "",
                request.getCategory() != null ? request.getCategory() : "",
                request.getStyle() != null ? request.getStyle() : "friendly",
                request.getTargetAudience() != null ? request.getTargetAudience() : "customers",
                request.getWordCount() != null ? request.getWordCount() : 500,
                "vi".equals(request.getLanguage()) ? "Việt" : "Anh",
                request.getAdditionalInstructions() != null ? request.getAdditionalInstructions() : ""
            );
            
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String content = response.getResult().getOutput().getText().trim();
            
            AIContentResponse result = new AIContentResponse();
            result.setContent(content);
            result.setExcerpt(generateExcerpt(content));
            result.setStatus("success");
            
            log.info("✅ AI generated content successfully, length: {} characters", content.length());
            return result;
            
        } catch (Exception e) {
            log.error("❌ Error generating content: {}", e.getMessage(), e);
            return createErrorResponse("Lỗi khi tạo nội dung: " + e.getMessage());
        }
    }


    private String buildPromptForCompleteContent(AIContentRequest request) {
        return String.format(
            "Bạn là chuyên gia viết nội dung cho Green Kitchen - một nhà hàng chuyên về thực phẩm sạch, an toàn và giàu dinh dưỡng.\n\n" +
            "## Thông tin về Green Kitchen\n" +
            "- **Địa chỉ**: 123 Nguyễn Văn Cừ, Q5, TP.HCM | Điện thoại: 0908 123 456\n" +
            "- **Thành lập**: Từ 2018\n" +
            "- **Bếp trưởng**: Nguyễn Thị Hạnh (trên 20 năm kinh nghiệm)\n" +
            "- **Thành tích**: Top 3 TP.HCM 2023, chứng nhận HACCP từ 2019, giải Vàng 2022, Doanh nghiệp vì cộng đồng 2021\n" +
            "- **Sứ mệnh**: Cung cấp thực phẩm sạch, an toàn, giàu dinh dưỡng cho cộng đồng\n" +
            "- **Giờ mở cửa**: 6:00 sáng đến 22:00 tối tất cả các ngày trong tuần\n\n" +
            "## Yêu cầu tạo nội dung\n" +
            "Hãy tạo nội dung bài viết với thông tin sau:\n\n" +
            "**Chủ đề**: %s\n" +
            "**Danh mục**: %s\n" +
            "**Phong cách viết**: %s\n" +
            "**Đối tượng độc giả**: %s\n" +
            "**Số từ mong muốn**: %d\n" +
            "**Ngôn ngữ**: %s\n" +
            "**Hướng dẫn bổ sung**: %s\n\n" +
            "## Yêu cầu trả về\n" +
            "Hãy tạo nội dung bài viết hoàn chỉnh và trả về theo format JSON chính xác như sau:\n\n" +
            "```json\n" +
            "{\n" +
            "  \"title\": \"Tiêu đề hấp dẫn và SEO-friendly phù hợp với chủ đề\",\n" +
            "  \"content\": \"Nội dung bài viết đầy đủ với các đoạn văn được cấu trúc rõ ràng, phù hợp với phong cách %s cho đối tượng %s. Bài viết khoảng %d từ và viết bằng %s.\",\n" +
            "  \"slug\": \"tieu-de-hap-dan-va-seo-friendly\",\n" +
            "  \"excerpt\": \"Tóm tắt ngắn gọn về nội dung bài viết trong 100-150 từ\",\n" +
            "  \"status\": \"success\"\n" +
            "}\n" +
            "```\n\n" +
            "**Lưu ý quan trọng:**\n" +
            "- Trả về CHÍNH XÁC format JSON như trên\n" +
            "- Không thêm text hoặc markdown khác\n" +
            "- Đảm bảo JSON hợp lệ\n" +
            "- Tiêu đề phải hấp dẫn và SEO-friendly\n" +
            "- Nội dung phải chất lượng cao, phù hợp với thương hiệu Green Kitchen\n" +
            "- Slug được tạo từ tiêu đề (lowercase, không dấu, dùng dấu gạch ngang)\n" +
            "- Excerpt là tóm tắt ngắn gọn trong 100-150 từ",
            request.getTopic() != null ? request.getTopic() : "",
            request.getCategory() != null ? request.getCategory() : "",
            request.getStyle() != null ? request.getStyle() : "friendly",
            request.getTargetAudience() != null ? request.getTargetAudience() : "customers",
            request.getWordCount() != null ? request.getWordCount() : 500,
            request.getLanguage() != null ? request.getLanguage() : "vi",
            request.getAdditionalInstructions() != null ? request.getAdditionalInstructions() : "",
            request.getStyle() != null ? request.getStyle() : "friendly",
            request.getTargetAudience() != null ? request.getTargetAudience() : "customers",
            request.getWordCount() != null ? request.getWordCount() : 500,
            request.getLanguage() != null ? request.getLanguage() : "vi"
        );
    }

    private AIContentResponse parseAIResponse(String content) {
        try {
            log.info("Parsing AI response: {}", content.substring(0, Math.min(200, content.length())));
            
            // Clean up the response - remove markdown code blocks if present
            String cleanContent = content.trim();
            if (cleanContent.startsWith("```json")) {
                cleanContent = cleanContent.substring(7);
            }
            if (cleanContent.endsWith("```")) {
                cleanContent = cleanContent.substring(0, cleanContent.length() - 3);
            }
            cleanContent = cleanContent.trim();
            
            // Try to parse as JSON first
            if (cleanContent.startsWith("{")) {
                AIContentResponse response = new AIContentResponse();
                
                // Extract title
                String title = extractJsonValue(cleanContent, "title");
                if (title != null && !title.trim().isEmpty()) {
                    response.setTitle(title.trim());
                    response.setSlug(generateSlug(title));
                }
                
                // Extract content
                String contentValue = extractJsonValue(cleanContent, "content");
                if (contentValue != null && !contentValue.trim().isEmpty()) {
                    response.setContent(contentValue.trim());
                    response.setExcerpt(generateExcerpt(contentValue));
                }
                
                // Extract slug if provided
                String slug = extractJsonValue(cleanContent, "slug");
                if (slug != null && !slug.trim().isEmpty()) {
                    response.setSlug(slug.trim());
                }
                
                // Extract excerpt if provided
                String excerpt = extractJsonValue(cleanContent, "excerpt");
                if (excerpt != null && !excerpt.trim().isEmpty()) {
                    response.setExcerpt(excerpt.trim());
                }
                
                // Extract status
                String status = extractJsonValue(cleanContent, "status");
                response.setStatus(status != null ? status : "success");
                
                // If we got both title and content, return success
                if (response.getTitle() != null && response.getContent() != null) {
                    return response;
                }
            }
            
            // If JSON parsing failed or incomplete, treat as plain content
            log.warn("JSON parsing failed or incomplete, treating as plain content");
            AIContentResponse response = new AIContentResponse();
            response.setContent(content);
            response.setExcerpt(generateExcerpt(content));
            response.setStatus("success");
            return response;
            
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, treating as plain content: {}", e.getMessage());
            AIContentResponse response = new AIContentResponse();
            response.setContent(content);
            response.setExcerpt(generateExcerpt(content));
            response.setStatus("success");
            return response;
        }
    }

    private String extractJsonValue(String json, String key) {
        try {
            // Handle both quoted and unquoted values, and multiline strings
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                String value = m.group(1);
                // Unescape common JSON escape sequences
                value = value.replace("\\n", "\n")
                           .replace("\\r", "\r")
                           .replace("\\t", "\t")
                           .replace("\\\"", "\"")
                           .replace("\\\\", "\\");
                return value;
            }
            
            // Try without quotes for simple values
            pattern = "\"" + key + "\"\\s*:\\s*([^,\\n\\r}]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(json);
            if (m.find()) {
                return m.group(1).trim().replaceAll("^\"|\"$", "");
            }
        } catch (Exception e) {
            log.warn("Failed to extract JSON value for key {}: {}", key, e.getMessage());
        }
        return null;
    }

    private String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim()
                .replaceAll("^-|-$", "");
    }

    private String generateExcerpt(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // Remove markdown formatting and HTML tags
        String cleanContent = content.replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("\\*([^*]+)\\*", "$1")
                .replaceAll("#+\\s*", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("\\n+", " ")
                .trim();
        
        // Take first 150 characters
        if (cleanContent.length() <= 150) {
            return cleanContent;
        }
        
        // Find last complete word within 150 characters
        String excerpt = cleanContent.substring(0, 150);
        int lastSpace = excerpt.lastIndexOf(' ');
        if (lastSpace > 100) { // Only if we have enough content
            excerpt = excerpt.substring(0, lastSpace);
        }
        
        return excerpt + "...";
    }

    private AIContentResponse createErrorResponse(String message) {
        AIContentResponse response = new AIContentResponse();
        response.setStatus("error");
        response.setMessage(message);
        return response;
    }
}
