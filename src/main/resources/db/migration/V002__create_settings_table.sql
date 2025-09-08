-- Create settings table
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    version INT DEFAULT 0,

    INDEX idx_setting_key (setting_key),
    INDEX idx_setting_type (setting_type),
    INDEX idx_is_active (is_active)
);

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, setting_type, is_active) VALUES
-- Shipping settings
('shipping.baseFee', '15000', 'shipping', true),
('shipping.freeShippingThreshold', '100000', 'shipping', true),
('shipping.additionalFeePerKm', '5000', 'shipping', true),
('shipping.maxDistance', '20', 'shipping', true),
('shipping.enabled', 'true', 'shipping', true),

-- Discount settings
('discounts.globalDiscount', '0', 'discounts', true),
('discounts.firstOrderDiscount', '10', 'discounts', true),
('discounts.loyaltyDiscount', '5', 'discounts', true),
('discounts.enabled', 'true', 'discounts', true),

-- Banner settings
('banners.homepageBanner.enabled', 'true', 'banners', true),
('banners.homepageBanner.title', '"Chào mừng đến với Green Kitchen"', 'banners', true),
('banners.homepageBanner.subtitle', '"Đồ ăn tươi ngon, giao tận nhà"', 'banners', true),
('banners.homepageBanner.buttonText', '"Đặt hàng ngay"', 'banners', true),
('banners.homepageBanner.buttonLink', '"/menu"', 'banners', true),
('banners.homepageBanner.imageUrl', '""', 'banners', true),

('banners.promotionBanner.enabled', 'false', 'banners', true),
('banners.promotionBanner.title', '"Khuyến mãi đặc biệt"', 'banners', true),
('banners.promotionBanner.subtitle', '"Giảm giá 20% cho đơn hàng đầu tiên"', 'banners', true),
('banners.promotionBanner.buttonText', '"Xem ngay"', 'banners', true),
('banners.promotionBanner.buttonLink', '"/promotions"', 'banners', true),
('banners.promotionBanner.imageUrl', '""', 'banners', true),

-- General settings
('general.siteName', '"Green Kitchen"', 'general', true),
('general.siteDescription', '"Đồ ăn tươi ngon, giao tận nhà"', 'general', true),
('general.contactEmail', '"contact@greenkitchen.vn"', 'general', true),
('general.contactPhone', '"+84 123 456 789"', 'general', true),
('general.maintenanceMode', 'false', 'general', true),
('general.maintenanceMessage', '"Website đang bảo trì, vui lòng quay lại sau."', 'general', true),

-- Business hours
('general.businessHours.monday.enabled', 'true', 'general', true),
('general.businessHours.monday.openTime', '"07:00"', 'general', true),
('general.businessHours.monday.closeTime', '"22:00"', 'general', true),
('general.businessHours.tuesday.enabled', 'true', 'general', true),
('general.businessHours.tuesday.openTime', '"07:00"', 'general', true),
('general.businessHours.tuesday.closeTime', '"22:00"', 'general', true),
('general.businessHours.wednesday.enabled', 'true', 'general', true),
('general.businessHours.wednesday.openTime', '"07:00"', 'general', true),
('general.businessHours.wednesday.closeTime', '"22:00"', 'general', true),
('general.businessHours.thursday.enabled', 'true', 'general', true),
('general.businessHours.thursday.openTime', '"07:00"', 'general', true),
('general.businessHours.thursday.closeTime', '"22:00"', 'general', true),
('general.businessHours.friday.enabled', 'true', 'general', true),
('general.businessHours.friday.openTime', '"07:00"', 'general', true),
('general.businessHours.friday.closeTime', '"22:00"', 'general', true),
('general.businessHours.saturday.enabled', 'true', 'general', true),
('general.businessHours.saturday.openTime', '"07:00"', 'general', true),
('general.businessHours.saturday.closeTime', '"22:00"', 'general', true),
('general.businessHours.sunday.enabled', 'true', 'general', true),
('general.businessHours.sunday.openTime', '"08:00"', 'general', true),
('general.businessHours.sunday.closeTime', '"21:00"', 'general', true),

-- Notification settings
('notifications.emailNotifications', 'true', 'notifications', true),
('notifications.smsNotifications', 'false', 'notifications', true),
('notifications.pushNotifications', 'true', 'notifications', true),
('notifications.orderConfirmations', 'true', 'notifications', true),
('notifications.deliveryUpdates', 'true', 'notifications', true),
('notifications.promotionalEmails', 'false', 'notifications', true),

-- Payment settings
('payment.codEnabled', 'true', 'payment', true),
('payment.onlinePaymentEnabled', 'true', 'payment', true),
('payment.paypalEnabled', 'false', 'payment', true),
('payment.stripeEnabled', 'false', 'payment', true),
('payment.paypalClientId', '""', 'payment', true),
('payment.stripePublishableKey', '""', 'payment', true),

-- Social media settings
('socialMedia.facebook', '""', 'socialMedia', true),
('socialMedia.instagram', '""', 'socialMedia', true),
('socialMedia.twitter', '""', 'socialMedia', true),
('socialMedia.youtube', '""', 'socialMedia', true),
('socialMedia.tiktok', '""', 'socialMedia', true),

-- SEO settings
('seo.metaTitle', '"Green Kitchen - Đồ ăn tươi ngon giao tận nhà"', 'seo', true),
('seo.metaDescription', '"Đặt đồ ăn tươi ngon từ Green Kitchen. Giao tận nhà nhanh chóng với chất lượng đảm bảo."', 'seo', true),
('seo.keywords', '"đồ ăn, giao tận nhà, tươi ngon, healthy food"', 'seo', true),
('seo.ogImage', '""', 'seo', true);
