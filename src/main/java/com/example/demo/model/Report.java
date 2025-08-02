package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // نوع حادثه مرتبط با گزارش
    private String incidentType;
    private ReportStatus status;
    // توضیحاتی که کاربر وارد می‌کند
    private String description;
    @Column(nullable = true)
private String address;
    // مختصات مکانی گزارش
    private Double latitude;
    private Double longitude;

    // مسیر فایل تصویر مرتبط با گزارش (در صورت وجود)
    private String imagePath;

    // تاریخ و زمان ثبت گزارش
    private LocalDateTime createdAt;

    // شماره تلفن کاربری که گزارش را ارسال کرده

    @Column(nullable = true)
    private String phone;

    // آیا این گزارش معتبر است یا خیر (بعداً توسط مسئول بررسی می‌شود)
    private Boolean valid = false;

    // وضعیت پردازش گزارش
    private Boolean processed = false;

    public Boolean getFake() {
        return isFake;
    }

    public void setFake(Boolean fake) {
        isFake = fake;
    }
    @Column(name = "resolved")
    private boolean resolved;
    private boolean isValid;
    private Boolean isFake = false;

    public Report() {
        this.createdAt = LocalDateTime.now();
    }

    // --- گتر و ستر‌ها ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getIncidentType() { return incidentType; }

    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getImagePath() { return imagePath; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Boolean getValid() { return valid; }

    public void setValid(Boolean valid) { this.valid = valid; }

    public Boolean getProcessed() { return processed; }

    public void setProcessed(Boolean processed) { this.processed = processed; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isValid () { return isValid; }
    public void setValid(boolean valid) { this.valid = valid; }


}



















//
//package com.example.demo.model;
//
//import com.example.demo.repository.ReportRepository;
//import jakarta.persistence.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "report")
//public class Report {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    private IncidentType incidentType;
//
//    private String description;
//    private double latitude;
//    private double longitude;
//    private boolean isValid;
//    @Enumerated(EnumType.STRING)
//    private ReportStatus status;
//
//    @ManyToOne
//    private User reporter;
//    @Column(name = "is_fake")
//    private boolean isFake; // گزارش دروغ یا واقعی
//
//    @Column(name = "resolved")
//    private boolean resolved; // به گزارش رسیدگی شده یا نه
//
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//    public Long getId() {
//        return id;
//    }
//    public void setId(Long id) {
//        this.id = id;
//    }
//    public IncidentType getIncidentType() {
//        return incidentType;
//    }
//    public void setIncidentType(IncidentType incidentType) {
//        this.incidentType = incidentType;
//    }
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String description) {
//        this.description = description;
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
//    public ReportStatus getStatus() {
//        return status;
//    }
//    public void setStatus(ReportStatus status) {
//        this.status = status;
//
//    }
//    public User getReporter() {
//        return reporter;
//    }
//    public void setReporter(User reporter) {
//        this.reporter = reporter;
//    }
//    @Column(name = "image_path")
//    private String imagePath;
//
//    public String getImagePath() {return imagePath;}
//
//    public void setImagePath(String imagePath) {this.imagePath = imagePath;}
//    public boolean isFake() {
//        return isFake;
//    }
//    public void setFake(boolean fake) {
//        isFake = fake;
//    }
//    public boolean isResolved() {
//        return resolved;
//    }
//    public void setResolved(boolean resolved) {
//        this.resolved = resolved;
//    }
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//public boolean isValid() {
//        return isValid;
//}
//public void setValid(boolean valid) {
//        isValid = valid;
//}
//
//}
