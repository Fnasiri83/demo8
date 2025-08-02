package com.example.demo.dto;

public class AuthResponse {
    private String token;
    private String role;

    public AuthResponse(String token, String role) {
        if (token == null || role == null) {
            throw new IllegalArgumentException("توکن و نقش نمی‌توانند null باشند");
        }
        this.token = token;
        this.role = role;
    }

    // Getter methods
    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    // Optional: Add toString() for better logging
    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='[PROTECTED]'" +
                ", role='" + role + '\'' +
                '}';
    }
}
//package com.example.demo.dto;
//public class AuthResponse {
//    private String token;
//    private String role ;
//    private Long userId;
//   private String username;
//    // Constructor جدید برای توکن و نقش
//    public AuthResponse(String token, String role) {
//        this.token = token;
//        this.role = role;
//        this.userId = userId;
//        this.username = username;
//
//    }
//    public String getToken() {return token;}
//    public void setToken(String token) {this.token = token;}
//    public String getRole() {return role;}
//    public void setRole(String role) {this.role = role;}
//    public Long getUserId() {return userId;}
//    public void setUserId(Long userId) {this.userId = userId;}
//    public String getUsername() {return username;}
//    public void setUsername(String username) {this.username = username;}
//
//
//}
