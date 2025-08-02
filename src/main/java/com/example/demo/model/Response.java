package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "responses") // نام جدول در پایگاه داده
public class Response {

    // شناسه یکتا و کلید اصلی
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ارتباط چندبه‌یک با مدل Report
    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    // محتوای پاسخ ارسالی
    @Column(columnDefinition = "TEXT")
    private String content;

    // نقش کاربر پاسخ‌دهنده (ADMIN, RESPONSE)
    private String role;

    // وضعیت پاسخ (ENUM می‌تواند مقادیر مختلفی داشته باشد)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    // نتیجه نهایی پاسخ (در صورت نیاز)
    private String result;

    // زمان ایجاد پاسخ (به صورت خودکار تنظیم می‌شود)
    private LocalDateTime createdAt = LocalDateTime.now();

    // زمان پاسخگویی (می‌تواند متفاوت از زمان ایجاد باشد)
    private LocalDateTime responseTime;

    // شناسه یا نام کاربر پاسخ‌دهنده
    private String responder;

    // وضعیت مشاهده پاسخ
    private boolean seen = false;

    // زمان مشاهده پاسخ
    private LocalDateTime seenAt;

    // مختصات جغرافیایی پاسخ (عرض جغرافیایی)
    private double latitude;

    // مختصات جغرافیایی پاسخ (طول جغرافیایی)
    private double longitude;

    // --- توابع دسترسی (Getters and Setters) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }

    public String getResponder() {
        return responder;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public LocalDateTime getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(LocalDateTime seenAt) {
        this.seenAt = seenAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}






//package com.example.demo.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "responses")
//public class Response {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // ارتباط با گزارش (Report)
//    @ManyToOne
//    @JoinColumn(name = "report_id", nullable = false)
//    private Report report;
//
//    // متنی که به عنوان پاسخ درج می‌شود
//    private String content;
//
//    // تاریخ و زمان ثبت پاسخ
//    private LocalDateTime createdAt;
//
//    // نام یا شناسه کاربر پاسخ‌دهنده (ادمین)
//    private String responder;
//
//    public Response() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//    // --- گتر و ستر‌ها ---
//
//    public Long getId() { return id; }
//
//    public void setId(Long id) { this.id = id; }
//
//    public Report getReport() { return report; }
//
//    public void setReport(Report report) { this.report = report; }
//
//    public String getContent() { return content; }
//
//    public void setContent(String content) { this.content = content; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public String getResponder() { return responder; }
//
//    public void setResponder(String responder) { this.responder = responder; }
//}
//
//
//
//
//
//
//
//
//







//package com.example.demo.model;
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//@Entity
//@Table(name = "respons")
//public class Response {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String role;
//    private String description;
//    @Enumerated(EnumType.STRING)
//    private ReportStatus status;
//    private String result;
//    @ManyToOne
//    private Report report;
//    private LocalDateTime responseTime;
//    private boolean seen;
//    private LocalDateTime seenAt;
//    private double latitude;
//    private double longitude;
//    public Long getId() {return id;}
//    public void setId(Long id) {this.id = id;}
//    public String getRole() {return role;}
//    public void setRole(String role) {this.role = role;}
//    public String getDescription() {return description;}
//    public void setDescription(String description) {this.description = description;}
//    public ReportStatus getStatus() {return status;}
//    public void setStatus(ReportStatus status) {
//        this.status = status;
//    }
//    public String getResult() {
//        return result;
//    }
//    public void setResult(String result) {
//        this.result = result;
//    }
//    public Report getReport() {
//        return report;
//    }
//    public void setReport(Report report) {
//        this.report = report;
//    }
//    public LocalDateTime getResponseTime() {
//        return responseTime;
//    }
//    public void setResponseTime(LocalDateTime responseTime) {
//        this.responseTime = responseTime;
//    }
//    public boolean isSeen() {
//        return seen;
//    }
//    public void setSeen(boolean seen) {
//        this.seen = seen;
//    }
//    public LocalDateTime getSeenAt() {
//        return seenAt;
//    }
//    public void setSeenAt(LocalDateTime seenAt) {
//        this.seenAt = seenAt;
//    }
//    public double getLatitude() {
//        return latitude;
//    }
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//    public double getLongitude() {
//        return longitude;
//    }
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//
//}
//
