package com.greenkitchen.portal.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.util.Scanner;

import com.greenkitchen.portal.dtos.AIContentRequest;
import com.greenkitchen.portal.dtos.AIContentResponse;
import com.greenkitchen.portal.dtos.AITopicsResponse;
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
            
            // Ưu tiên dùng template .md trong resources nếu có
            String prompt;
            boolean usedMarkdownTemplate = false;
            try {
                prompt = buildPromptFromMarkdownTemplate(request);
                usedMarkdownTemplate = true;
            } catch (Exception ex) {
                log.warn("Markdown prompt template not found or invalid, fallback to manual prompt: {}", ex.getMessage());
                prompt = buildPromptForCompleteContent(request);
            }
            
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            String content = response.getResult().getOutput().getText();
            log.info("✅ AI generated content successfully, length: {} characters", content.length());
            
            AIContentResponse aiResp = parseAIResponse(content);
            aiResp.setPromptSource(usedMarkdownTemplate ? "markdown" : "manual");
            return aiResp;
            
        } catch (Exception e) {
            log.error("❌ Error generating post content: {}", e.getMessage(), e);
            return createErrorResponse("Lỗi khi tạo nội dung: " + e.getMessage());
        }
    }

    @Override
    public AITopicsResponse suggestTopics(String category, String style, String audience, int count, String language) {
        try {
            String prompt = String.format(
                "Hãy đề xuất %d chủ đề bài viết phù hợp cho nhà hàng Green Kitchen về danh mục '%s', phong cách '%s', đối tượng '%s', ngôn ngữ '%s'. " +
                "Chỉ trả về danh sách chủ đề, mỗi dòng một chủ đề, không thêm chú thích.",
                Math.max(3, Math.min(count, 15)),
                category != null ? category : "",
                style != null ? style : "friendly",
                audience != null ? audience : "customers",
                language != null ? language : "vi"
            );

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String text = response.getResult().getOutput().getText();
            java.util.List<String> topics = new java.util.ArrayList<>();
            for (String line : text.split("\n")) {
                String t = line.replaceAll("^[-*#0-9.\\s]+", "").trim();
                if (!t.isBlank()) topics.add(t);
            }
            AITopicsResponse resp = new AITopicsResponse();
            resp.setTopics(topics);
            resp.setStatus("success");
            return resp;
        } catch (Exception e) {
            AITopicsResponse resp = new AITopicsResponse();
            resp.setStatus("error");
            resp.setMessage(e.getMessage());
            resp.setTopics(java.util.List.of());
            return resp;
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
                "QUAN TRỌNG: Chèn 1-3 hình ảnh phù hợp với chủ đề bằng cú pháp markdown dạng: ![Mô tả ảnh](URL_ảnh). " +
                "Ưu tiên ảnh chất lượng cao từ Unsplash/Pexels (ví dụ: https://images.unsplash.com/photo-1512621776951-a57141f2eefd, https://images.unsplash.com/photo-1546554137-f86b9593a222). " +
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
            String before = content;
            content = ensureImagesInContent(request.getTopic(), request.getCategory(), content);
            
            AIContentResponse result = new AIContentResponse();
            result.setContent(content);
            result.setExcerpt(generateExcerpt(content));
            result.setStatus("success");
            result.setPromptSource("manual");
            if (before.equals(content)) {
                result.setImageInjection("ai");
            } else {
                result.setImageInjection("fallback");
            }
            
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
            "  \"content\": \"Nội dung bài viết đầy đủ với các đoạn văn được cấu trúc rõ ràng, phù hợp với phong cách %s cho đối tượng %s. Bài viết khoảng %d từ và viết bằng %s. QUAN TRỌNG: Bao gồm 1-3 hình ảnh phù hợp với chủ đề sử dụng markdown format: ![Mô tả ảnh](URL_ảnh). Chọn ảnh từ danh sách được cung cấp hoặc ảnh tương tự từ Unsplash.\",\n" +
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
            "- Excerpt là tóm tắt ngắn gọn trong 100-150 từ\n" +
            "- Hình ảnh phải sử dụng markdown format: ![Mô tả](URL_ảnh) và chọn ảnh phù hợp chủ đề (ví dụ từ Unsplash: https://images.unsplash.com/photo-1512621776951-a57141f2eefd, https://images.unsplash.com/photo-1546554137-f86b9593a222)",
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

    private String buildPromptFromMarkdownTemplate(AIContentRequest request) throws Exception {
        ClassPathResource resource = new ClassPathResource("prompts/PostContentGeneration.md");
        if (!resource.exists()) throw new IllegalStateException("Template PostContentGeneration.md not found");
        try (InputStream is = resource.getInputStream(); Scanner s = new Scanner(is, StandardCharsets.UTF_8)) {
            s.useDelimiter("\\A");
            String tpl = s.hasNext() ? s.next() : "";
            if (tpl.isBlank()) throw new IllegalStateException("Template is empty");

            // Thay thế placeholder theo định dạng trong file .md
            tpl = tpl.replace("{topic}", safe(request.getTopic()))
                     .replace("{category}", safe(request.getCategory()))
                     .replace("{style}", safeOrDefault(request.getStyle(), "friendly"))
                     .replace("{targetAudience}", safeOrDefault(request.getTargetAudience(), "customers"))
                     .replace("{wordCount}", String.valueOf(request.getWordCount() != null ? request.getWordCount() : 500))
                     .replace("{language}", safeOrDefault(request.getLanguage(), "vi"))
                     .replace("{additionalInstructions}", safeOrDefault(request.getAdditionalInstructions(), ""));
            return tpl;
        }
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String safeOrDefault(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
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
                    String original = contentValue.trim();
                    String finalized = ensureImagesInContent(null, null, original);
                    response.setContent(finalized);
                    response.setExcerpt(generateExcerpt(finalized));
                    response.setImageInjection(original.equals(finalized) ? "ai" : "fallback");
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
            String original = content;
            String finalized = ensureImagesInContent(null, null, original);
            response.setContent(finalized);
            response.setExcerpt(generateExcerpt(finalized));
            response.setStatus("success");
            response.setImageInjection(original.equals(finalized) ? "ai" : "fallback");
            return response;
            
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, treating as plain content: {}", e.getMessage());
            AIContentResponse response = new AIContentResponse();
            String original = content;
            String finalized = ensureImagesInContent(null, null, original);
            response.setContent(finalized);
            response.setExcerpt(generateExcerpt(finalized));
            response.setStatus("success");
            response.setImageInjection(original.equals(finalized) ? "ai" : "fallback");
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

    private String ensureImagesInContent(String topic, String category, String content) {
        try {
            if (content == null || content.isBlank()) return content;
            // If already contains markdown image, return as-is
            if (content.matches("(?s).*!\\[[^\\]]*]\\([^)]*\\).*") ) {
                return content;
            }

            java.util.List<String> urls = pickImageUrls(topic, category, content);
            if (urls.isEmpty()) return content;

            String injected = injectImagesIntoContent(content, urls);
            return injected;
        } catch (Exception ex) {
            return content;
        }
    }

    private String injectImagesIntoContent(String content, java.util.List<String> urls) {
        // Chèn ảnh ngay sau các đoạn có từ khóa phù hợp; nếu không tìm thấy thì chèn cuối
        String[] paragraphs = content.split("\r?\n\r?\n+");
        if (paragraphs.length == 0) return content;

        java.util.Set<Integer> usedIdx = new java.util.HashSet<>();
        int urlIdx = 0;

        String[] keywordGroups = new String[]{
            // Ưu tiên chèn sau đoạn nói về món ăn/healthy/rau quả
            "salad|healthy|rau|dinh dưỡng|nutrition|vitamin|khoáng|bữa ăn|món ăn",
            // Công thức/nấu ăn
            "công thức|recipe|nấu ăn|cooking|bếp",
            // Không gian nhà hàng/Green Kitchen
            "nhà hàng|restaurant|không gian|Green Kitchen|dịch vụ",
            // Người đang nấu ăn / đầu bếp
            "đang nấu|người nấu|đầu bếp|chef|cook|cooking",
            // Tâm trạng khó chịu / stress
            "khó chịu|stress|căng thẳng|mệt mỏi|lo âu",
            // Gia đình hạnh phúc
            "gia đình|family|hạnh phúc|trẻ em|kids"
        };

        for (String group : keywordGroups) {
            if (urlIdx >= urls.size()) break;
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(group, java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);
            for (int i = 0; i < paragraphs.length && urlIdx < urls.size(); i++) {
                if (usedIdx.contains(i)) continue;
                String para = paragraphs[i];
                if (p.matcher(para).find()) {
                    paragraphs[i] = para + "\n\n" + "![Green Kitchen](" + urls.get(urlIdx) + ")";
                    usedIdx.add(i);
                    urlIdx++;
                }
            }
        }

        // Nếu vẫn còn URL chưa chèn, thêm vào cuối bài
        if (urlIdx < urls.size()) {
            StringBuilder last = new StringBuilder(paragraphs[paragraphs.length - 1]);
            for (; urlIdx < urls.size(); urlIdx++) {
                last.append("\n\n").append("![Green Kitchen](").append(urls.get(urlIdx)).append(")");
            }
            paragraphs[paragraphs.length - 1] = last.toString();
        }

        String rebuilt = String.join("\n\n", paragraphs);
        log.debug("Injected images contextually; total: {}", urls.size());
        return rebuilt;
    }

    private java.util.List<String> pickImageUrls(String topic, String category, String content) {
        java.util.List<String> result = new java.util.ArrayList<>();
        String ctx = ((topic != null ? topic : "") + " " + (category != null ? category : "") + " " + (content != null ? content : "")).toLowerCase();

        if (ctx.contains("salad") || ctx.contains("healthy") || ctx.contains("salad") || ctx.contains("rau") ) {
            result.add("https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=80");
        } else if (ctx.contains("nấu ăn") || ctx.contains("cooking") || ctx.contains("công thức") || ctx.contains("recipe")) {
            result.add("https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1600891964599-f61ba0e24092?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1589307004394-7a54af3a5f34?auto=format&fit=crop&w=1200&q=80");
        } else if (ctx.contains("trái cây") || ctx.contains("fruit") || ctx.contains("smoothie")) {
            result.add("https://images.unsplash.com/photo-1601004890684-d8cbf643f5f2?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1510626176961-4b57d4fbad03?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1502741126161-b048400d1161?auto=format&fit=crop&w=1200&q=80");
        } else if (ctx.contains("nhà hàng") || ctx.contains("restaurant") || ctx.contains("không gian")) {
            result.add("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1514933651103-005eec06c04b?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1400&q=80");
        } else if (ctx.contains("khó chịu") || ctx.contains("stress") || ctx.contains("căng thẳng") || ctx.contains("mệt mỏi") || ctx.contains("lo âu")) {
            // Tâm trạng khó chịu / stress
            result.add("https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1510022079733-8b58aca7c4ae?auto=format&fit=crop&w=1200&q=80");
        } else if (ctx.contains("gia đình") || ctx.contains("family") || ctx.contains("hạnh phúc") || ctx.contains("trẻ em") || ctx.contains("kids")) {
            // Gia đình hạnh phúc
            result.add("https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=1200&q=80");
        } else {
            // Fallback to vegetables/organic
            result.add("https://images.unsplash.com/photo-1546554137-f86b9593a222?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=1200&q=80");
            result.add("https://images.unsplash.com/photo-1610832958506-aa56368176cf?auto=format&fit=crop&w=1200&q=80");
        }

        return result;
    }
}
