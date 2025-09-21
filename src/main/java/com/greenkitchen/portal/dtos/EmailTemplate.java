package com.greenkitchen.portal.dtos;

public class EmailTemplate {
    private String subject;
    private String content;
    private String holidayName;
    private String holidayDate;
    private String templateType;

    public EmailTemplate() {}

    public EmailTemplate(String subject, String content, String holidayName, String holidayDate, String templateType) {
        this.subject = subject;
        this.content = content;
        this.holidayName = holidayName;
        this.holidayDate = holidayDate;
        this.templateType = templateType;
    }

    // Getters and Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }

    public String getHolidayDate() { return holidayDate; }
    public void setHolidayDate(String holidayDate) { this.holidayDate = holidayDate; }

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public static class Builder {
        private String subject;
        private String content;
        private String holidayName;
        private String holidayDate;
        private String templateType;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder holidayName(String holidayName) {
            this.holidayName = holidayName;
            return this;
        }

        public Builder holidayDate(String holidayDate) {
            this.holidayDate = holidayDate;
            return this;
        }

        public Builder templateType(String templateType) {
            this.templateType = templateType;
            return this;
        }

        public EmailTemplate build() {
            return new EmailTemplate(subject, content, holidayName, holidayDate, templateType);
        }
    }
}
