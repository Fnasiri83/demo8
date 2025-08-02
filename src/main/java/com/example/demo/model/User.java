package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // نام کاربری یکتا برای ورود یا شناسایی کاربر... اجباری
    @Column(nullable = false, unique = true)
    private String username;

    // رمز عبور (در نسخه نهایی باید رمزنگاری شود) ...اجباری
    @Column(nullable = false)
    private String password;

    //  اختیاری ...شماره تلفن کاربر - برای تماس یا ارسال گزارش
    @Column(nullable = true)
    private String phone;

    // نقش کاربر - برای مدیریت سطح دسترسی
    @Enumerated(EnumType.STRING)
    private Role role;
     // مختصات جغرافیایی کاربر
    @Column(nullable = true)
    private Double latitude;
    @Column(nullable = true)
    private Double longitude;

    // وضعیت فعال بودن حساب کاربر
    private boolean active = true;

    @Column(name = "image_path")
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // --- گتر و ستر‌ها ---

    public Long getId   () {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}






















//package com.example.demo.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "users")
//public class User {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true)
//    private String phone;
//private String password;
//    private String username; // لاگین با این
//
//    private String role;
//
//    private double latitude;
//    private double longitude;
//
//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//    public void setId(Long id) {
//        this.id = id;
//    }
//    public String getPhone() {
//        return phone;
//    }
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//    public String getUsername() {
//        return username;
//    }
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getRole() {
//        return role;
//    }
//    public void setRole(String role) {
//        this.role = role;
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
//    public String getPassword() {
//        return password;
//    }
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//}
