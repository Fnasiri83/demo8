package com.example.demo.controller;
import com.example.demo.model.User;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.AuthResponse;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.UUID;
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
 /**
     * ثبت‌نام اولیه کاربر
     * ورودی: یک DTO با اطلاعات ثبت‌نام
     * خروجی: پیام موفقیت و توکن JWT
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto registrationDto)
    {
        System.out.println("wwwwwwwwwwwwwww");
        System.out.println("wwwwwwwwwwwwwww");
        System.out.println("wwwwwwwwwwwwwww");

        try {
            // بررسی اینکه نقش فرستاده شده یا نه
            if (registrationDto.getRole() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "نقش کاربر باید مشخص شود (مثلاً: ADMIN یا RESCUER یا USER)",
                        "status", "error"
                ));
            }

            // ثبت کاربر جدید (در سرویس)
            System.out.println("bbbbbb");
            User registeredUser = userService.registerUser(registrationDto);
            System.out.println(registeredUser.getUsername());

            // بازگشت پیام موفقیت و اطلاعات کاربر به صورت Map
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "ثبت‌نام موفقیت‌آمیز بود",
                            "username", registeredUser.getUsername(),
                            "status", "success"
                    ));
        } catch (IllegalArgumentException e) {
            // مدیریت خطاهای اعتبارسنجی ورودی
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", e.getMessage(),
                            "status", "error"
                    ));
        }
    }


    /**
     * به‌روزرسانی موقعیت جغرافیایی کاربر
     * فقط نقش‌های ADMIN و RESPONSE دسترسی دارند
     */
    @PostMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> updateLocation(@PathVariable Long id,
                                            @RequestParam double latitude,
                                            @RequestParam double longitude) {
        // پیدا کردن کاربر با استفاده از Optional
        Optional<User> userOpt = userService.findById(id);
        // نکته: بهتر است findById استفاده شود!
        if (userOpt.isEmpty()) {
            // اگر کاربر پیدا نشد، پیام خطا برگردان
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("کاربر پیدا نشد");
        }

        User user = userOpt.get();
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        User updated = userService.saveUser(user);
        return ResponseEntity.ok(updated);
    }

    /**
     * دریافت لیست همه کاربران (فقط برای ادمین)
     * خروجی: List<User> (لیست کاربران)
     * کاربرد List: نگهداری مجموعه‌ای از اشیاء (کاربران)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * دریافت کاربر بر اساس نام کاربری (فقط ادمین)
     * خروجی: Optional<User> (ممکن است کاربر وجود نداشته باشد)
     * کاربرد Optional: جلوگیری از NullPointerException و مدیریت وضعیت عدم وجود داده
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username,
                                               @RequestHeader("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
        }

        Optional<User> userOpt = userService.findByUsername(username);
        // اگر کاربر وجود داشت، برگردان؛ اگر نه، 404
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * دریافت کاربر بر اساس شماره تلفن (ADMIN و RESPONSE)
     * خروجی: Optional<User>
     */
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<?> getUserByPhone(@PathVariable String phone,
                                            @RequestHeader("role") String role) {
        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("RESPONSE"))) {
            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
        }

        Optional<User> userOpt = userService.findByPhone(phone);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * ویرایش اطلاعات کاربر (فقط ادمین)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody User userDetails,
                                        @RequestHeader("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
        }
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * حذف کاربر (فقط ادمین)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        @RequestHeader("role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * لاگین (ورود) کاربران خاص (مثال ساده)
     * خروجی: توکن JWT و نقش کاربر
     */
    @PostMapping("/auth/login")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("دریافت درخواست ورود برای کاربر: {}", loginRequest.getUsername());

        try {
            // مرحله 1: بررسی ورودی
            if (StringUtils.isEmpty(loginRequest.getUsername()) ||
                    StringUtils.isEmpty(loginRequest.getPassword())) {
                log.warn("ورودی نامعتبر - نام کاربری یا رمز عبور خالی است");
                return ResponseEntity.badRequest().body(Map.of(
                        "پیغام", "نام کاربری و رمز عبور الزامی است",
                        "وضعیت", "خطا"
                ));
            }

            // مرحله 2: جستجوی کاربر
            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                log.warn("کاربر یافت نشد: {}", loginRequest.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "پیغام", "نام کاربری یا رمز عبور اشتباه است",
                        "وضعیت", "خطا"
                ));
            }

            User user = userOpt.get();


// خطوط دیباگ پیشنهادی
            System.out.println("Username: " + loginRequest.getUsername());
            System.out.println("Password: " + loginRequest.getPassword());
            System.out.println("User from DB: " + user.getUsername());
            System.out.println("Encoded password in DB: " + user.getPassword());
            System.out.println("Password match: " + passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()));

// مرحله 3: بررسی رمز عبور
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("رمز عبور نادرست برای کاربر: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "پیغام", "نام کاربری یا رمز عبور اشتباه است",
                        "وضعیت", "خطا"
                ));
            }


            log.debug("اطلاعات کاربر یافت شد: {}", user.getUsername());

            // مرحله 3: بررسی رمز عبور
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("رمز عبور نادرست برای کاربر: {}", user.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "پیغام", "نام کاربری یا رمز عبور اشتباه است",
                        "وضعیت", "خطا"
                ));
            }

            // مرحله 4: بررسی نقش
            if (user.getRole() == null) {
                log.warn("نقش برای کاربر تعریف نشده است: {}", user.getUsername());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "پیغام", "نقش کاربر مشخص نشده است. لطفاً ابتدا ثبت‌نام را کامل انجام دهید.",
                        "وضعیت", "خطا"
                ));
            }

            String roleName = user.getRole().name();
            log.debug("نقش کاربر تعیین شد: {}", roleName);

            // مرحله 5: تولید توکن
            String token;
            try {
                token = jwtUtil.generateToken(user.getUsername(), roleName);
                log.debug("توکن با موفقیت تولید شد");
            } catch (Exception e) {
                log.error("خطا در تولید توکن", e);
                return ResponseEntity.internalServerError().body(Map.of(
                        "پیغام", "خطا در تولید توکن امنیتی",
                        "وضعیت", "خطا"
                ));
            }

            // مرحله 6: پاسخ موفق
            AuthResponse response = new AuthResponse(token, roleName);
            log.info("ورود موفقیت‌آمیز برای کاربر: {}", user.getUsername());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("خطای غیرمنتظره در سیستم ورود", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "پیغام", "خطای داخلی در سرور",
                    "وضعیت", "خطا",
                    "جزئیات", e.getMessage() != null ? e.getMessage() : "خطای نامشخص"
            ));
        }
    }

}








//@PostMapping("/{userId}/upload-image")
//    @CrossOrigin(origins = "http://localhost:4200")
//    public ResponseEntity<?> uploadImage(@PathVariable Long userId,
//                                         @RequestParam("file") MultipartFile file) {
//        User updatedUser = userService.uploadUserImage(userId, file);
//
//        return ResponseEntity.ok(Map.of(
//                "message", "تصویر با موفقیت ذخیره شد",
//                "imagePath", updatedUser.getImagePath()
//        ));
//    }
//
//    @PostMapping("/upload")
//    @CrossOrigin(origins = "http://localhost:4200")
//    public ResponseEntity<Object> manualUpload(@RequestParam("file") MultipartFile multipartFile) {
//        try {
//            // مسیر مطلق ذخیره‌سازی، همسو با application.properties
//            String uploadDir = "C:/Users/Dolphin/Desktop/image";
//
//            // ساخت نام یکتا برای فایل
//            String originalFilename = multipartFile.getOriginalFilename();
//            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
//            String filePath = uploadDir + File.separator + uniqueFilename;
//
//            // ایجاد فایل و نوشتن محتوا
//            File convertFile = new File(filePath);
//            convertFile.createNewFile();
//
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(multipartFile.getBytes());
//            }
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "فایل با موفقیت ذخیره شد",
//                    "imagePath", "/images/" + uniqueFilename
//            ));
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "message", "خطا در ذخیره‌سازی فایل",
//                    "error", e.getMessage()
//            ));
//        }
//    }
//    @GetMapping("/images/{filename:.+}")
//    @CrossOrigin(origins = "http://localhost:4200")
//    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
//        try {
//            Resource image = userService.loadUserImage(filename);
//
//            // تشخیص نوع فایل (اختیاری ولی پیشنهاد می‌شود)
//            String contentType = Files.probeContentType(Paths.get("C:/Users/Dolphin/Desktop/image/" + filename));
//            if (contentType == null) {
//                contentType = "application/octet-stream"; // fallback
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(contentType))
//                    .body(image);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//    }


//    @GetMapping("/images/{filename:.+}")
//    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
//        Resource image = userService.loadUserImage(filename);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .body(image);
//    }



//    @GetMapping("/images/{filename:.+}")
//    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
//        Resource image = userService.loadUserImage(filename);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG) // یا IMAGE_PNG بسته به فایل
//                .body(image);
//        @PostMapping("/{userId}/upload-image")
//    @CrossOrigin(origins = "http://localhost:4200")
//        public ResponseEntity<?> uploadImage(@PathVariable Long userId,
//                                             @RequestParam("file") MultipartFile file) {
//            User updatedUser = userService.uploadUserImage(userId, file);
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "تصویر با موفقیت ذخیره شد",
//                    "imagePath", updatedUser.getImagePath()
//            ));
//        }
//
//        // نمایش تصویر
//        @GetMapping("/images/{filename:.+}")
//        public ResponseEntity<Resource> getImage(@PathVariable String filename) {
//            Resource image = userService.loadUserImage(filename);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_JPEG) // یا IMAGE_PNG بسته به نوع فایل
//                    .body(image);
//        }
















//        catch (Exception e) {
//             مدیریت سایر خطاها
//            return ResponseEntity.internalServerError()
//                    .body(Map.of(
//                            "message", "خطا در ثبت‌نام: " + e.getMessage(),
//                            "status", "error"
//                    ));
//        }






























//    @PostMapping("/auth/login")
//    @CrossOrigin(origins = "http://localhost:4200")
//
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        System.out.println(1234567);
//        try {
//            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
//
//            if (userOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("message", "نام کاربری یا رمز عبور اشتباه است", "status", "error"));
//            }
//            User user = userOpt.get();
//            System.out.println("Username: " + loginRequest.getUsername());
//            System.out.println("Password: " + loginRequest.getPassword());
//            System.out.println("User from DB: " + user.getUsername());
//            System.out.println("Encoded password in DB: " + user.getPassword());
//            System.out.println("Password match: " + passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()));
//
//
//            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("message", "نام کاربری یا رمز عبور اشتباه است", "status", "error"));
//            }
//
//            String token = jwtUtil.generateToken(
//                    user.getUsername(),
//                    user.getRole().name()
//            );
//
//
//            // ساخت پاسخ موفقیت‌آمیز
//
//            return ResponseEntity.ok(new AuthResponse(
//                    token,
//                    user.getRole().name()
//
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body(Map.of("message", "خطا در ورود به سیستم", "status", "error"));
//        }
//
//
//    }


//            // تعیین نقش کاربر با مقدار پیش‌فرض USER اگر نقش null بود
//            String roleName = (user.getRole() != null) ? user.getRole().name() : "USER";
//
//            // تولید توکن JWT
//            String token = jwtUtil.generateToken(user.getUsername(), roleName);
//        @ExceptionHandler(MethodArgumentNotValidException.class)
//        public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
//            Map<String, String> errors = new HashMap<>();
//            ex.getBindingResult().getFieldErrors().forEach(error ->
//                    errors.put(error.getField(), error.getDefaultMessage()));
//            return ResponseEntity.badRequest().body(errosr);
//        }

//    @PostMapping("/auth/login")
//    @CrossOrigin(origins = "http://localhost:4200")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        try {
//            Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
//
//            if (userOpt.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("message", "نام کاربری یا رمز عبور اشتباه است", "status", "error"));
//            }
//
//            User user = userOpt.get();
//
//            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(Map.of("message", "نام کاربری یا رمز عبور اشتباه است", "status", "error"));
//            }
//
//            // بخش تولید توکن JWT فعلاً کامنت شد:
//        /*
//        String token = jwtUtil.generateToken(
//                user.getUsername(),
//                user.getRole().name()
//        );
//
//        return ResponseEntity.ok(new AuthResponse(
//                token,
//                user.getRole().name(),
//                user.getId(),
//                user.getUsername()
//        ));
//        */
//
//            // پاسخ ساده بدون توکن برای تست کلاینت:
//            return ResponseEntity.ok(Map.of(
//                    "role", user.getRole().name(),
//                    "id", user.getId(),
//                    "username", user.getUsername()
//            ));
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                    .body(Map.of("message", "خطا در ورود به سیستم", "status", "error"));
//        }
//    }



//}  @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        // لیست کاربرهای مجاز برای ورود (مثال ساده)
//        Map<String, String> allowedUsers = Map.of(
//                "admin", "ADMIN",
//                "response", "RESPONSE",
//                "user"  ,  "USER",
//
//
//                );
//
//        // بررسی نام کاربری و رمز عبور
//        if (allowedUsers.containsKey(loginRequest.getUsername()) &&
//                loginRequest.getPassword().equals("1234")) {
//
//            String role = allowedUsers.get(loginRequest.getUsername());
//            String token = jwtUtil.generateToken(loginRequest.getUsername());
//            return ResponseEntity.ok(new AuthResponse(token,role));
//        }
//
//        // اگر اطلاعات صحیح نبود
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body("فقط مدیران و مسئولان پاسخگو می‌توانند وارد شوند");
//    }
//    @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        // لیست کاربرهای مجاز برای ورود (مثال ساده)
//        Map<String, String> allowedUsers = Map.of(
//                "admin", "ADMIN",
//                "response", "RESPONSE",
//                "user"  ,  "USER",
//
//
//        );

//        // بررسی نام کاربری و رمز عبور
//        if (allowedUsers.containsKey(loginRequest.getUsername()) &&
//                loginRequest.getPassword().equals("1234")) {
//
//            String role = allowedUsers.get(loginRequest.getUsername());
//            String token = jwtUtil.generateToken(loginRequest.getUsername());
//            return ResponseEntity.ok(new AuthResponse(token,role));
//        }
//
//        // اگر اطلاعات صحیح نبود
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body("فقط مدیران و مسئولان پاسخگو می‌توانند وارد شوند");
//    }
//}










//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    // سازنده کنترلر با تزریق وابستگی UserService
//    public UserController(UserRepository userRepository,
//                          PasswordEncoder passwordEncoder) {
//        this.userService = userService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    /**
//     * ثبت‌نام اولیه اختیاری
//     */
//    @PostMapping("/register")
////    @CrossOrigin(origins = "http://localhost:4200")
//    public ResponseEntity<?> register(@RequestBody UserRegistrationDto registrationDto) {
//        try {
//            User registeredUser = userService.registerUser(registrationDto);
//
//            return ResponseEntity.ok()
//                    .body(Map.of(
//                            "message", "ثبت‌نام موفقیت‌آمیز بود",
//                            "username", registeredUser.getUsername()
//                    ));
//        } catch (IllegalArgumentException e) {
//            // خطای مربوط به نام کاربری تکراری
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            // سایر خطاهای سیستمی
//            return ResponseEntity.internalServerError()
//                    .body("خطا در ثبت‌نام: " + e.getMessage());
//        }
//    }
//
//
//        /**
//         * به‌روزرسانی موقعیت جغرافیایی کاربر بر اساس شناسه
//         * @param id شناسه کاربر
//         * @param latitude عرض جغرافیایی
//         * @param longitude طول جغرافیایی
//         * @return کاربر با موقعیت به‌روزرسانی شده یا خطا در صورت عدم یافتن کاربر
//         */
//    @PostMapping("/{id}/location")
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> updateLocation(@PathVariable Long id,
//                                            @RequestParam double latitude,
//                                            @RequestParam double longitude) {
//        Optional<User> userOpt = userService.findByUsername(id.toString());
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("کاربر پیدا نشد");
//        }
//        User user = userOpt.get();
//        user.setLatitude(latitude);
//        user.setLongitude(longitude);
//        User updated = userService.saveUser(user);
//        return ResponseEntity.ok(updated);
//    }
//
//    /**
//     * دریافت تمام کاربران (فقط برای مدیر سیستم)
//     * @param role نقش کاربر درخواست دهنده از هدر
//     * @return لیست کاربران یا خطای دسترسی
//     */
//    @GetMapping
//    public ResponseEntity<Object> getAllUsers(@RequestHeader("role") String role) {
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    /**
//     * دریافت کاربر بر اساس نام کاربری (فقط برای مدیر سیستم)
//     * @param username نام کاربری مورد جستجو
//     * @param role نقش کاربر درخواست دهنده
//     * @return اطلاعات کاربر یا خطا در صورت عدم یافتن یا دسترسی
//     */
//    @GetMapping("/by-username/{username}")
//    public ResponseEntity<?> getUserByUsername(@PathVariable String username,
//                                               @RequestHeader("role") String role) {
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByUsername(username);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    /**
//     * دریافت کاربر بر اساس شماره تلفن (برای مدیر و مسئول پاسخگو)
//     * @param phone شماره تلفن مورد جستجو
//     * @param role نقش کاربر درخواست دهنده
//     * @return اطلاعات کاربر یا خطا در صورت عدم یافتن یا دسترسی
//     */
//    @GetMapping("/by-phone/{phone}")
//    public ResponseEntity<?> getUserByPhone(@PathVariable String phone,
//                                            @RequestHeader("role") String role) {
//        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("RESPONSE"))) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByPhone(phone);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    /**
//     * به‌روزرسانی اطلاعات کاربر (فقط برای مدیر سیستم)
//     * @param id شناسه کاربر
//     * @param userDetails اطلاعات جدید کاربر
//     * @param role نقش کاربر درخواست دهنده
//     * @return کاربر به‌روزرسانی شده یا خطای دسترسی
//     */
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateUser(@PathVariable Long id,
//                                        @RequestBody User userDetails,
//                                        @RequestHeader("role") String role) {
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        User updatedUser = userService.updateUser(id, userDetails);
//        return ResponseEntity.ok(updatedUser);
//    }
//
//    /**
//     * حذف کاربر (فقط برای مدیر سیستم)
//     * @param id شناسه کاربر
//     * @param role نقش کاربر درخواست دهنده
//     * @return پاسخ بدون محتوا در صورت موفقیت یا خطای دسترسی
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteUser(@PathVariable Long id,
//                                        @RequestHeader("role") String role) {
//        if (!"ADMIN".equalsIgnoreCase(role)) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    /**
//     * ورود به سیستم و دریافت توکن احراز هویت
//     * @param loginRequest درخواست حاوی نام کاربری و رمز عبور
//     * @return توکن JWT در صورت موفقیت یا خطای احراز هویت
//     */
//    @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        // لیست کاربران مجاز به همراه نقش‌های آنها (به صورت سخت‌کد شده)
//        Map<String, String> allowedUsers = Map.of(
//                "admin", "ADMIN",
//                "response", "RESPONSE"
//        );
//
//        // بررسی وجود کاربر در لیست مجاز
//        if (allowedUsers.containsKey(loginRequest.getUsername()) &&
//                loginRequest.getPassword().equals("1234")) {
//
//            String role = allowedUsers.get(loginRequest.getUsername());
//            String token = jwtUtil.generateToken(loginRequest.getUsername());
//            return ResponseEntity.ok(new AuthResponse(token,role));
//        }
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                .body("فقط مدیران و مسئولان پاسخگو می‌توانند وارد شوند");
//    }
//}
//
//
//
//









/**
 * لاگین با نام کاربری یا شماره تلفن
 */
//    @PostMapping("/auth/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        Optional<User> userOpt = userService.findByUsernameOrPhone(
//                loginRequest.getUsernameOrPhone(), loginRequest.getUsernameOrPhone()
//        );
//
//        if (userOpt.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("کاربری یافت نشد");
//        }
//
//        User user = userOpt.get();
//        if (!user.getPassword().equals(loginRequest.getPassword())) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("رمز عبور اشتباه است");
//        }
//
//        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
//        return ResponseEntity.ok(new AuthResponse(token));
//    }


//    @PostMapping("/register")
//    public ResponseEntity<User> registerUser(@RequestBody User user,
//                                             @RequestParam double latitude,
//                                             @RequestParam double longitude) {
//        user.setLatitude(latitude);
//        user.setLongitude(longitude);
//        User savedUser = userService.saveUser(user);
//        return ResponseEntity.ok(savedUser);
//    }


//package com.example.demo.controller;
//
//import com.example.demo.model.User;
//import com.example.demo.service.UserService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    private final UserService userService;
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    // ثبت کاربر جدید همراه با مختصات جغرافیایی
//    @PostMapping("/register")
//    public ResponseEntity<User> registerUser(@RequestBody User user,
//                                             @RequestParam double latitude,
//                                             @RequestParam double longitude) {
//        user.setLatitude(latitude);
//        user.setLongitude(longitude);
//        User savedUser = userService.saveUser(user);
//        return ResponseEntity.ok(savedUser);
//    }
//
//    // مشاهده تمام کاربران (فقط توسط ادمین)
//    @GetMapping
//    public ResponseEntity<Object> getAllUsers(@RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    // جستجو بر اساس نام کاربری (فقط ادمین)
//    @GetMapping("/by-username/{username}")
//    public ResponseEntity<?> getUserByUsername(@PathVariable String username,
//                                               @RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByUsername(username);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // جستجو بر اساس شماره تلفن (ادمین و امدادگر)
//    @GetMapping("/by-phone/{phone}")
//    public ResponseEntity<?> getUserByPhone(@PathVariable String phone,
//                                            @RequestHeader("role") String role) {
//        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("RESPONSE"))) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByPhone(phone);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // بروزرسانی اطلاعات کاربر
//    @PutMapping("/{id}")
//    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
//        User updatedUser = userService.updateUser(id, userDetails);
//        return ResponseEntity.ok(updatedUser);
//    }
//
//    // حذف کاربر
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.noContent().build();
//    }
//}
//
//
//


































//package com.example.demo.controller;
//
//import com.example.demo.model.Role;
//import com.example.demo.model.User;
//import com.example.demo.service.UserService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;





















//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    private final UserService userService;
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    // ۱. ثبت کاربر جدید همراه با مختصات جغرافیایی (دسترسی آزاد)
//    @PostMapping("/register")
//    public ResponseEntity<User> registerUser(@RequestBody User user,
//                                             @RequestParam double latitude,
//                                             @RequestParam double longitude) {
//        user.setLatitude(latitude);       // 📍 ثبت عرض جغرافیایی
//        user.setLongitude(longitude);     // 📍 ثبت طول جغرافیایی
//        User savedUser = userService.saveUser(user);
//        return ResponseEntity.ok(savedUser);
//    }
//
//    // ۲. مشاهده تمام کاربران (فقط توسط ادمین)
//    @GetMapping
//    public ResponseEntity<Object> getAllUsers(@RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    // ۳. جستجو بر اساس نام کاربری (فقط ادمین)
//    @GetMapping("/by-username/{username}")
//    public ResponseEntity<?> getUserByUsername(@PathVariable String username,
//                                               @RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByUsername(username);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // ۴. جستجو بر اساس شماره تلفن (ادمین و امدادگر)
//    @GetMapping("/by-phone/{phone}")
//    public ResponseEntity<?> getUserByPhone(@PathVariable String phone,
//                                            @RequestHeader("role") String role) {
//        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("RESPONSE"))) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByPhone(phone);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//
//    }
//    // 🔐 تابع بررسی نقش‌ها
//    private boolean isAdmin(String phone) {
//        return userService.findByPhone(phone)
//                .map(user -> Role.ADMIN.equals(user.getRole()))
//                .orElse(false);
//    }
//
//}

























//package com.example.demo.controller;
//
//import com.example.demo.model.User;
//import com.example.demo.service.UserService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/users")
//public class UserController {
//
//    private final UserService userService;
//
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    // ۱. ثبت کاربر جدید (دسترسی آزاد)
//    @PostMapping("/register")
//    public ResponseEntity<User> registerUser(@RequestBody User user) {
//        User savedUser = userService.saveUser(user);
//        return ResponseEntity.ok(savedUser);
//    }
//
//    // ۲. مشاهده تمام کاربران (فقط توسط ادمین)
//    @GetMapping
//    public ResponseEntity<?> getAllUsers(@RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    // ۳. جستجو بر اساس نام کاربری (فقط ادمین)
//    @GetMapping("/by-username/{username}")
//    public ResponseEntity<?> getUserByUsername(@PathVariable String username,
//                                               @RequestHeader("role") String role) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByUsername(username);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // ۴. جستجو بر اساس شماره تلفن (ادمین و امدادگر)
//    @GetMapping("/by-phone/{phone}")
//    public ResponseEntity<?> getUserByPhone(@PathVariable String phone,
//                                            @RequestHeader("role") String role) {
//        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("RESPONSE"))) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//
//        Optional<User> userOpt = userService.findByPhone(phone);
//        return userOpt.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//}
