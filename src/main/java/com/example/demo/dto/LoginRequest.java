package com.example.demo.dto;
public class LoginRequest {
    private String username;
    private String password;
    private String role;
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

}
/**
 * برای لاگین با نام کاربری یا شماره تلفن
 */
//public class LoginRequest {
//    private String usernameOrPhone;
//    private String password;
//
//    public String getUsernameOrPhone() {
//        return usernameOrPhone;
//    }
//
//    public void setUsernameOrPhone(String usernameOrPhone) {
//        this.usernameOrPhone = usernameOrPhone;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//}
