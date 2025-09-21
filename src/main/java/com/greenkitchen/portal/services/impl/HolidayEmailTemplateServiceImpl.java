package com.greenkitchen.portal.services.impl;

import com.greenkitchen.portal.dtos.EmailTemplate;
import com.greenkitchen.portal.dtos.HolidayDto;
import com.greenkitchen.portal.entities.Holiday;
import com.greenkitchen.portal.services.HolidayEmailTemplateService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class HolidayEmailTemplateServiceImpl implements HolidayEmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public EmailTemplate generateTemplate(Holiday holiday) {
        String holidayName = holiday.getName().toLowerCase();
        
        if (holidayName.contains("tết") || holidayName.contains("tet") || holidayName.contains("nguyên đán")) {
            return generateTetTemplate(holiday);
        } else if (holidayName.contains("black friday")) {
            return generateBlackFridayTemplate(holiday);
        } else if (holidayName.contains("thanksgiving")) {
            return generateThanksgivingTemplate(holiday);
        } else if (holidayName.contains("cyber monday")) {
            return generateCyberMondayTemplate(holiday);
        } else if (holidayName.contains("quốc khánh") || holidayName.contains("national day")) {
            return generateNationalDayTemplate(holiday);
        } else if (holidayName.contains("giáng sinh") || holidayName.contains("christmas")) {
            return generateChristmasTemplate(holiday);
        } else if (holidayName.contains("valentine")) {
            return generateValentineTemplate(holiday);
        } else if (holidayName.contains("phụ nữ") || holidayName.contains("women")) {
            return generateWomensDayTemplate(holiday);
        } else {
            return generateGenericHolidayTemplate(holiday);
        }
    }

    @Override
    public EmailTemplate generateTemplate(Holiday holiday, String templateType) {
        return switch (templateType.toLowerCase()) {
            case "tet" -> generateTetTemplate(holiday);
            case "black_friday" -> generateBlackFridayTemplate(holiday);
            case "thanksgiving" -> generateThanksgivingTemplate(holiday);
            case "cyber_monday" -> generateCyberMondayTemplate(holiday);
            case "national_day" -> generateNationalDayTemplate(holiday);
            case "christmas" -> generateChristmasTemplate(holiday);
            case "valentine" -> generateValentineTemplate(holiday);
            case "womens_day" -> generateWomensDayTemplate(holiday);
            default -> generateGenericHolidayTemplate(holiday);
        };
    }

    @Override
    public String[] getAvailableTemplateTypes() {
        return new String[]{
            "tet", "black_friday", "thanksgiving", "cyber_monday", 
            "national_day", "christmas", "valentine", "womens_day", "generic"
        };
    }

    private EmailTemplate generateTetTemplate(Holiday holiday) {
        String subject = "🎊 Chúc Mừng Năm Mới - Ưu Đãi Đặc Biệt Từ Green Kitchen!";
        String content = buildTetContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "tet");
    }

    private EmailTemplate generateBlackFridayTemplate(Holiday holiday) {
        String subject = "🛍️ BLACK FRIDAY - Siêu Sale Lên Đến 50% Tại Green Kitchen!";
        String content = buildBlackFridayContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "black_friday");
    }

    private EmailTemplate generateThanksgivingTemplate(Holiday holiday) {
        String subject = "🦃 Thanksgiving Special - Cảm Ơn Quý Khách Hàng!";
        String content = buildThanksgivingContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "thanksgiving");
    }

    private EmailTemplate generateCyberMondayTemplate(Holiday holiday) {
        String subject = "💻 CYBER MONDAY - Deal Sốc Chỉ Có Online!";
        String content = buildCyberMondayContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "cyber_monday");
    }

    private EmailTemplate generateNationalDayTemplate(Holiday holiday) {
        String subject = "🇻🇳 Chào Mừng Ngày Quốc Khánh - Ưu Đãi Đặc Biệt!";
        String content = buildNationalDayContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "national_day");
    }

    private EmailTemplate generateChristmasTemplate(Holiday holiday) {
        String subject = "🎄 Merry Christmas - Quà Tặng Đặc Biệt Từ Green Kitchen!";
        String content = buildChristmasContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "christmas");
    }

    private EmailTemplate generateValentineTemplate(Holiday holiday) {
        String subject = "💕 Valentine's Day - Bữa Tối Lãng Mạn Cho Cặp Đôi!";
        String content = buildValentineContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "valentine");
    }

    private EmailTemplate generateWomensDayTemplate(Holiday holiday) {
        String subject = "👩 Ngày Quốc Tế Phụ Nữ - Tôn Vinh Vẻ Đẹp Tự Nhiên!";
        String content = buildWomensDayContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "womens_day");
    }

    private EmailTemplate generateGenericHolidayTemplate(Holiday holiday) {
        String subject = "🎉 " + holiday.getName() + " - Ưu Đãi Đặc Biệt Từ Green Kitchen!";
        String content = buildGenericHolidayContent(holiday);
        return new EmailTemplate(subject, content, holiday.getName(), 
            holiday.getDate().format(DATE_FORMATTER), "generic");
    }

    // Content builders
    private String buildTetContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #ff6b6b, #ffa726); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">🎊 Chúc Mừng Năm Mới 2025! 🎊</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Từ Green Kitchen với tình yêu thương</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #2e7d32; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Chúc mừng năm mới! Chúng tôi hy vọng năm 2025 sẽ mang đến cho bạn và gia đình nhiều niềm vui, sức khỏe và hạnh phúc.</p>
                    
                    <p>Nhân dịp <strong>Tết Nguyên Đán</strong> ({{holidayDate}}), Green Kitchen xin gửi đến bạn những ưu đãi đặc biệt:</p>
                    
                    <div style="background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #d32f2f; margin-top: 0;">🎁 Ưu Đãi Tết Đặc Biệt:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>Giảm 20%</strong> cho tất cả đơn hàng từ 500,000 VNĐ</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 300,000 VNĐ</li>
                            <li><strong>Combo Tết</strong> với giá ưu đãi đặc biệt</li>
                            <li><strong>Quà tặng</strong> voucher 50,000 VNĐ cho đơn hàng tiếp theo</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #4CAF50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🛒 Đặt Món Ngay
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc bạn và gia đình một năm mới an khang, thịnh vượng!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildBlackFridayContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #000; padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 32px; color: #ff4444;">🛍️ BLACK FRIDAY</h1>
                    <p style="font-size: 18px; margin: 10px 0; color: #ffcc00;">SIÊU SALE LÊN ĐẾN 50%!</p>
                    <p style="font-size: 14px; margin: 5px 0;">{{holidayDate}} - Chỉ trong 24 giờ!</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #000; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Black Friday đã đến! Đây là cơ hội duy nhất trong năm để bạn sở hữu những món ăn ngon nhất với giá tốt nhất!</p>
                    
                    <div style="background: #ff4444; color: white; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center;">
                        <h3 style="margin: 0; font-size: 24px;">⚡ DEAL SỐC ⚡</h3>
                        <p style="margin: 10px 0; font-size: 18px;">Giảm giá lên đến <strong>50%</strong></p>
                        <p style="margin: 5px 0; font-size: 14px;">Áp dụng cho tất cả sản phẩm</p>
                    </div>
                    
                    <div style="background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #d32f2f; margin-top: 0;">🔥 Ưu Đãi Đặc Biệt:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>50% OFF</strong> - Combo Healthy Meal</li>
                            <li><strong>40% OFF</strong> - Tất cả món chính</li>
                            <li><strong>30% OFF</strong> - Đồ uống & Tráng miệng</li>
                            <li><strong>Miễn phí ship</strong> cho mọi đơn hàng</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #ff4444; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🛒 MUA NGAY - SỐ LƯỢNG CÓ HẠN!
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 12px; text-align: center; margin-top: 30px;">
                        *Ưu đãi có hiệu lực từ 00:00 đến 23:59 ngày {{holidayDate}}<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildThanksgivingContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #8B4513, #D2691E); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">🦃 Happy Thanksgiving!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Cảm ơn bạn đã tin tưởng Green Kitchen</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #8B4513; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Thanksgiving là thời điểm để chúng ta bày tỏ lòng biết ơn. Chúng tôi muốn cảm ơn bạn đã là khách hàng thân thiết của Green Kitchen!</p>
                    
                    <div style="background: #fff3e0; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ff9800;">
                        <h3 style="color: #e65100; margin-top: 0;">🙏 Lời Cảm Ơn Đặc Biệt:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>20% OFF</strong> cho tất cả đơn hàng</li>
                            <li><strong>Combo Thanksgiving</strong> với giá ưu đãi</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 200,000 VNĐ</li>
                            <li><strong>Quà tặng</strong> món tráng miệng miễn phí</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #ff9800; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🍽️ Thưởng Thức Bữa Tối
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Cảm ơn bạn đã đồng hành cùng chúng tôi!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildCyberMondayContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #2196F3, #21CBF3); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">💻 CYBER MONDAY</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Deal Sốc Chỉ Có Online!</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #1976D2; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Cyber Monday - ngày hội mua sắm online lớn nhất năm! Chỉ có trên website, không có tại cửa hàng!</p>
                    
                    <div style="background: #e3f2fd; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #1976D2; margin-top: 0;">💻 Ưu Đãi Cyber Monday:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>35% OFF</strong> - Tất cả món ăn healthy</li>
                            <li><strong>25% OFF</strong> - Combo gia đình</li>
                            <li><strong>Miễn phí ship</strong> cho mọi đơn hàng online</li>
                            <li><strong>Gift card</strong> 100,000 VNĐ cho đơn hàng từ 1,000,000 VNĐ</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #2196F3; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            💻 MUA ONLINE NGAY
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 12px; text-align: center; margin-top: 30px;">
                        *Chỉ áp dụng cho đơn hàng online ngày {{holidayDate}}<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildNationalDayContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #ff0000, #ffff00); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">🇻🇳 Chào Mừng Ngày Quốc Khánh!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Tự hào là người Việt Nam</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #d32f2f; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Nhân dịp <strong>Ngày Quốc Khánh Việt Nam</strong> ({{holidayDate}}), Green Kitchen xin gửi lời chúc mừng đến toàn thể đồng bào!</p>
                    
                    <div style="background: #fff3e0; padding: 20px; border-radius: 5px; margin: 20px 0; border: 2px solid #ff9800;">
                        <h3 style="color: #d32f2f; margin-top: 0;">🇻🇳 Ưu Đãi Quốc Khánh:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>15% OFF</strong> cho tất cả đơn hàng</li>
                            <li><strong>Combo Việt Nam</strong> với giá đặc biệt</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 300,000 VNĐ</li>
                            <li><strong>Quà tặng</strong> cờ Tổ quốc cho mỗi đơn hàng</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #d32f2f; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🍽️ Thưởng Thức Món Việt
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc mừng ngày Quốc Khánh Việt Nam!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildChristmasContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #2e7d32, #4caf50); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">🎄 Merry Christmas!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Chúc mừng Giáng Sinh an lành</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #2e7d32; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Merry Christmas! Chúc bạn và gia đình một mùa Giáng Sinh ấm áp, hạnh phúc và tràn đầy niềm vui!</p>
                    
                    <div style="background: #e8f5e8; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #4caf50;">
                        <h3 style="color: #2e7d32; margin-top: 0;">🎁 Ưu Đãi Giáng Sinh:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>25% OFF</strong> cho tất cả đơn hàng</li>
                            <li><strong>Combo Giáng Sinh</strong> với giá ưu đãi</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 400,000 VNĐ</li>
                            <li><strong>Quà tặng</strong> bánh Giáng Sinh miễn phí</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #4caf50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🎄 Đặt Món Giáng Sinh
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc bạn một mùa Giáng Sinh an lành và hạnh phúc!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildValentineContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #e91e63, #f8bbd9); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">💕 Happy Valentine's Day!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Bữa tối lãng mạn cho cặp đôi</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #e91e63; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Valentine's Day là ngày đặc biệt để thể hiện tình yêu thương. Hãy để Green Kitchen giúp bạn tạo nên một bữa tối lãng mạn hoàn hảo!</p>
                    
                    <div style="background: #fce4ec; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #e91e63; margin-top: 0;">💕 Combo Valentine Đặc Biệt:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>Combo Cặp Đôi</strong> với giá ưu đãi 30%</li>
                            <li><strong>Món ăn lãng mạn</strong> được chế biến đặc biệt</li>
                            <li><strong>Trang trí đặc biệt</strong> cho bữa tối</li>
                            <li><strong>Quà tặng</strong> chocolate handmade</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #e91e63; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            💕 Đặt Bàn Lãng Mạn
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc bạn một Valentine's Day ngọt ngào!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildWomensDayContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #ff69b4, #ffb6c1); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">👩 Ngày Quốc Tế Phụ Nữ!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Tôn vinh vẻ đẹp tự nhiên</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #e91e63; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Nhân dịp <strong>Ngày Quốc Tế Phụ Nữ</strong> ({{holidayDate}}), Green Kitchen xin gửi lời chúc mừng đến tất cả những người phụ nữ tuyệt vời!</p>
                    
                    <div style="background: #fce4ec; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #e91e63; margin-top: 0;">👩 Ưu Đãi Đặc Biệt Cho Phụ Nữ:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>20% OFF</strong> cho tất cả phụ nữ</li>
                            <li><strong>Combo Healthy</strong> dành riêng cho phụ nữ</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 250,000 VNĐ</li>
                            <li><strong>Quà tặng</strong> món tráng miệng healthy</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #e91e63; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            👩 Thưởng Thức Món Healthy
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc mừng ngày Quốc Tế Phụ Nữ!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    private String buildGenericHolidayContent(Holiday holiday) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #4caf50, #8bc34a); padding: 20px; border-radius: 10px;">
                <div style="text-align: center; color: white; margin-bottom: 30px;">
                    <h1 style="margin: 0; font-size: 28px;">🎉 {{holidayName}}!</h1>
                    <p style="font-size: 16px; margin: 10px 0;">Ưu đãi đặc biệt từ Green Kitchen</p>
                </div>
                
                <div style="background: white; padding: 25px; border-radius: 8px; margin-bottom: 20px;">
                    <h2 style="color: #2e7d32; margin-top: 0;">Dear {{customerName}},</h2>
                    
                    <p>Nhân dịp <strong>{{holidayName}}</strong> ({{holidayDate}}), Green Kitchen xin gửi đến bạn những ưu đãi đặc biệt!</p>
                    
                    <div style="background: #e8f5e8; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #2e7d32; margin-top: 0;">🎁 Ưu Đãi Đặc Biệt:</h3>
                        <ul style="color: #333; line-height: 1.6;">
                            <li><strong>15% OFF</strong> cho tất cả đơn hàng</li>
                            <li><strong>Miễn phí ship</strong> cho đơn hàng từ 300,000 VNĐ</li>
                            <li><strong>Combo đặc biệt</strong> với giá ưu đãi</li>
                            <li><strong>Quà tặng</strong> món tráng miệng miễn phí</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="{{frontendUrl}}/menu" style="background: #4caf50; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; font-size: 16px; display: inline-block;">
                            🛒 Đặt Món Ngay
                        </a>
                    </div>
                    
                    <p style="color: #666; font-size: 14px; text-align: center; margin-top: 30px;">
                        Chúc bạn một ngày {{holidayName}} vui vẻ!<br>
                        <strong>Green Kitchen Team</strong>
                    </p>
                </div>
            </div>
            """;
    }

    @Override
    public EmailTemplate generateTemplate(HolidayDto holidayDto) {
        // Convert DTO to Entity for template generation
        Holiday holiday = convertDtoToEntity(holidayDto);
        return generateTemplate(holiday);
    }

    @Override
    public EmailTemplate generateTemplate(HolidayDto holidayDto, String templateType) {
        // Convert DTO to Entity for template generation
        Holiday holiday = convertDtoToEntity(holidayDto);
        return generateTemplate(holiday, templateType);
    }

    /**
     * Convert HolidayDto to Holiday entity
     */
    private Holiday convertDtoToEntity(HolidayDto dto) {
        Holiday holiday = new Holiday();
        holiday.setId(dto.id);
        holiday.setName(dto.name);
        holiday.setCountry(dto.country);
        holiday.setDate(dto.date);
        holiday.setLunar(dto.lunar);
        holiday.setRecurrenceType(Holiday.RecurrenceType.valueOf(dto.recurrenceType));
        holiday.setDescription(dto.description);
        return holiday;
    }
}
