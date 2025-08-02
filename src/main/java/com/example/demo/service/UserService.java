package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path root = Paths.get("uploads");


    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        try {
            Files.createDirectories(root); // ایجاد پوشه اگر وجود نداشت
        } catch (IOException e) {
            throw new RuntimeException("خطا در ایجاد پوشه آپلود", e);
        }
    }


    /**
     * ذخیره یا بروزرسانی اطلاعات کاربر
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * جستجوی کاربر با شناسه
     */
    public Optional<User> findById (Long id) {
        return userRepository.findById(id);
    }



    /**
     * جستجوی کاربر با شماره تلفن
     */
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * دریافت لیست همه کاربران
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * حذف کاربر با آیدی
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * بروزرسانی اطلاعات کاربر مشخص‌شده
     */
    public User updateUser(Long id, User updatedData) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedData.getUsername());
                    user.setPhone(updatedData.getPhone());
                    user.setRole(updatedData.getRole());
                    user.setLatitude(updatedData.getLatitude());
                    user.setLongitude(updatedData.getLongitude());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("کاربر پیدا نشد"));
    }
    public User registerUser(UserRegistrationDto registrationDto) {
        // بررسی وجود کاربر با این نام کاربری
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("نام کاربری قبلا ثبت شده است");
        }

        // ایجاد کاربر جدید
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : Role.USER);
        user.setPhone(registrationDto.getPhone());

        return userRepository.save(user);
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean validateUserRole(String username, String requiredRole) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getRole().equals(requiredRole);
        }
        return false;
    }



}












// آپلود عکس کاربر
//    public User uploadUserImage(Long userId, MultipartFile file) {
//        try {
//            Optional<User> optionalUser = userRepository.findById(userId);
//            if (optionalUser.isEmpty()) {
//                throw new RuntimeException("کاربر پیدا نشد");
//            }
//
//            User user = optionalUser.get();
//
//            String uploadDir = "C:/Users/Dolphin/Desktop/image";
//            String originalFilename = file.getOriginalFilename();
//            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
//            String filePath = uploadDir + File.separator + uniqueFilename;
//
//            File convertFile = new File(filePath);
//            convertFile.createNewFile();
//
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(file.getBytes());
//            }
//
//            user.setImagePath("/images/" + uniqueFilename);
//            return userRepository.save(user);
//        } catch (IOException e) {
//            throw new RuntimeException("خطا در ذخیره فایل", e);
//        }
//    }
//    // نمایش عکس
//    public Resource loadUserImage(String filename) {
//        try {
//            File file = new File("C:/Users/Dolphin/Desktop/image/" + filename);
//            if (!file.exists() || !file.canRead()) {
//                throw new RuntimeException("فایل یافت نشد یا قابل خواندن نیست");
//            }
//            return new UrlResource(file.toURI());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException("خطا در بارگذاری تصویر", e);
//        }
//    }
//    // ثبت‌نام کاربر جدید
//    public User registerUser(UserRegistrationDto registrationDto) {
//        if (userRepository.existsByUsername(registrationDto.getUsername())) {
//            throw new IllegalArgumentException("نام کاربری قبلاً ثبت شده است");
//        }
//
//        User user = new User();
//        user.setUsername(registrationDto.getUsername());
//        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
//        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : Role.USER);
//        user.setPhone(registrationDto.getPhone());
//
//        return userRepository.save(user);
//    }
//
//    public Optional<User> findById(Long id) {
//        return userRepository.findById(id);
//    }
//    public User saveUser(User user) {
//        return userRepository.save(user);
//    }
//
//
//    public Optional<User> findByPhone(String phone) {
//        return userRepository.findByPhone(phone);
//    }
//
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    public void deleteUser(Long id) {
//        userRepository.deleteById(id);
//    }
//
//    public User updateUser(Long id, User updatedData) {
//        return userRepository.findById(id)
//                .map(user -> {
//                    user.setUsername(updatedData.getUsername());
//                    user.setPhone(updatedData.getPhone());
//                    user.setRole(updatedData.getRole());
//                    user.setLatitude(updatedData.getLatitude());
//                    user.setLongitude(updatedData.getLongitude());
//                    return userRepository.save(user);
//                })
//                .orElseThrow(() -> new RuntimeException("کاربر پیدا نشد"));
//    }
//
//    public boolean validateUserRole(String username, String requiredRole) {
//        return userRepository.findByUsername(username)
//                .map(user -> user.getRole().equals(requiredRole))
//                .orElse(false);
//    }

//package com.example.demo.service;
//import org.springframework.core.io.Resource;
//
//
//import com.example.demo.dto.UserRegistrationDto;
//import com.example.demo.model.Role;
//import com.example.demo.model.User;
//import com.example.demo.repository.UserRepository;
//
//import org.springframework.core.io.UrlResource;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.core.io.UrlResource;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository userRepository,
//                       PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    /**
//     * ذخیره یا بروزرسانی اطلاعات کاربر
//     */
//    public User saveUser(User user) {
//        return userRepository.save(user);
//    }
//
//    /**
//     * جستجوی کاربر با شناسه
//     */
//    public Optional<User> findById (Long id) {
//        return userRepository.findById(id);
//    }
//
//
//
//    /**
//     * جستجوی کاربر با شماره تلفن
//     */
//    public Optional<User> findByPhone(String phone) {
//        return userRepository.findByPhone(phone);
//    }
//
//    /**
//     * دریافت لیست همه کاربران
//     */
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    /**
//     * حذف کاربر با آیدی
//     */
//    public void deleteUser(Long id) {
//        userRepository.deleteById(id);
//    }
//
//    /**
//     * بروزرسانی اطلاعات کاربر مشخص‌شده
//     */
//    public User updateUser(Long id, User updatedData) {
//        return userRepository.findById(id)
//                .map(user -> {
//                    user.setUsername(updatedData.getUsername());
//                    user.setPhone(updatedData.getPhone());
//                    user.setRole(updatedData.getRole());
//                    user.setLatitude(updatedData.getLatitude());
//                    user.setLongitude(updatedData.getLongitude());
//                    return userRepository.save(user);
//                })
//                .orElseThrow(() -> new RuntimeException("کاربر پیدا نشد"));
//    }
//    public User registerUser(UserRegistrationDto registrationDto) {
//        // بررسی وجود کاربر با این نام کاربری
//        if (userRepository.existsByUsername(registrationDto.getUsername())) {
//            throw new IllegalArgumentException("نام کاربری قبلا ثبت شده است");
//        }
//
//        // ایجاد کاربر جدید
//        User user = new User();
//        user.setUsername(registrationDto.getUsername());
//        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
//        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : Role.USER);
//        user.setPhone(registrationDto.getPhone());
//
//        return userRepository.save(user);
//    }
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public boolean validateUserRole(String username, String requiredRole) {
//        Optional<User> userOpt = userRepository.findByUsername(username);
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            return user.getRole().equals(requiredRole);
//        }
//        return false;
//    }
//    private final Path root = Paths.get("uploads"); // پوشه‌ی ذخیره عکس‌ها
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//        try {
//            Files.createDirectories(root); // اگر پوشه وجود نداشت، بسازه
//        } catch (IOException e) {
//            throw new RuntimeException("مشکل در ایجاد پوشه آپلود", e);
//        }
//    }
//
//    public User uploadUserImage(Long userId, MultipartFile file) {
//        try {
//            Optional<User> optionalUser = userRepository.findById(userId);
//            if (optionalUser.isEmpty()) {
//                throw new RuntimeException("کاربر پیدا نشد");
//            }
//
//            User user = optionalUser.get();
//
//            // نام فایل یکتا
//            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path filePath = this.root.resolve(filename);
//
//            // ذخیره فایل
//            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//            // ذخیره مسیر در دیتابیس
//            user.setImagePath("/images/" + filename); // برای مصرف سمت کلاینت
//            return userRepository.save(user);
//
//        } catch (IOException e) {
//            throw new RuntimeException("خطا در ذخیره فایل", e);
//        }
//    }
//
//    public Resource loadUserImage(String filename) {
//        try {
//            Path file = root.resolve(filename);
//            Resource resource = new UrlResource(file.toUri());
//
//            if (resource.exists() || resource.isReadable()) {
//                return resource;
//            } else {
//                throw new RuntimeException("فایل پیدا نشد یا قابل خواندن نیست");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("خطا در بارگذاری تصویر", e);
//        }
//    }
//}
//
//


























//package com.example.demo.service;
//
//import com.example.demo.dto.AuthResponse;
//import com.example.demo.model.User;
//import com.example.demo.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final BCryptPasswordEncoder passwordEncoder;
//    @Autowired
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = new BCryptPasswordEncoder(); // هش‌گر رمز عبور
//    }
//    public class LoginRequest {
//        private String usernameOrPhone;
//        private String password;
//
////        // getters و setters
////        public String getUsernameOrPhone() {
////            return usernameOrPhone;
////        }
//
//        public void setUsernameOrPhone(String usernameOrPhone) {
//            this.usernameOrPhone = usernameOrPhone;
//        }
//
//        public Optional<User> findByUsernameOrPhone(String username, String phone) {
//            return userRepository.findByUsernameOrPhone(username, phone);
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//    }
//
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//
//    public Optional<User> findByPhone(String phone) {
//        return userRepository.findByPhone(phone);
//    }
//
//    public User saveUser(User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword())); // هش قبل از ذخیره
//        return userRepository.save(user);
//    }
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    public User updateUser(Long id, User userDetails) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        user.setUsername(userDetails.getUsername());
//        user.setPhone(userDetails.getPhone());
//        user.setRole(userDetails.getRole());
//        user.setLatitude(userDetails.getLatitude());
//        user.setLongitude(userDetails.getLongitude());
//
//        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
//            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
//        }
//
//        return userRepository.save(user);
//    }
//
//    public void deleteUser(Long id) {
//        userRepository.deleteById(id);
//    }
//    public void deleteAllUsers() {
//        userRepository.deleteAll();
//    }
//
//}
//
//
//

















//package com.example.demo.service;
//import com.example.demo.repository.UserRepository;
//import com.example.demo.model.Report;
//import com.example.demo.model.User;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//
//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public Optional<User> findByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//public List<Report> findByreport(String report) {
//        return findByreport(report);
//}
//    public Optional<User> findByPhone(String phone) {
//        return userRepository.findByPhone(phone);
//    }
//
//    public User saveUser(User user) {
//        return userRepository.save(user);
//    }
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    public User updateUserRole(Long id, String newRole) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        user.setRole(newRole);
//        return userRepository.save(user);
//    }
//}
