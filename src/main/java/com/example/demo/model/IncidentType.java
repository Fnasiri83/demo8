package com.example.demo.model;
import jakarta.persistence.*;
@Entity
@Table(name = "incident")
public class IncidentType {
    public IncidentType() {
        // سازنده پیش‌فرض برای JPA
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "icon_path")
    private String iconPath;
    private String code; // مثل FIRE, FLOOD, OVERGRAZING
    private String title; // عنوان فارسی مثل "آتش‌سوزی"
    // Constructors
    public IncidentType(String code, String title, String iconPath) {
        this.code = code;
        this.title = title;
        this.iconPath = iconPath;
    }
    // Getters and Setters
    public Long getId() {return id;}
    public String getCode() {return code;}
    public void setCode(String code) {this.code = code;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getIconPath() {return iconPath;}
    public void setIconPath(String iconPath) {this.iconPath = iconPath;}
}

