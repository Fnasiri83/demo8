package com.example.demo.controller;

import com.example.demo.model.IncidentType;
import com.example.demo.model.Role;
import com.example.demo.service.IncidentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/incident-types")
public class IncidentTypeController {

    private final IncidentTypeService incidentTypeService;

    /**
     * سازنده کنترلر که سرویس IncidentType را از طریق تزریق وابستگی دریافت می‌کند
     * @param incidentTypeService سرویس مربوط به مدیریت نوع حوادث
     */
    public IncidentTypeController(IncidentTypeService incidentTypeService) {
        this.incidentTypeService = incidentTypeService;
    }

    /**
     * ایجاد یک نوع حادثه جدید در سیستم
     * @param incidentType اطلاعات نوع حادثه که از بدنه درخواست دریافت می‌شود
     * @param roleStr نقش کاربر که از هدر درخواست خوانده می‌شود
     * @return پاسخ HTTP شامل نوع حادثه ایجاد شده یا پیام خطا
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> createIncidentType(@RequestBody IncidentType incidentType,
                                                @RequestHeader("role") String roleStr) {
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            IncidentType created = incidentTypeService.createIncidentType(incidentType, role);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("نقش ارسالی معتبر نیست");
        } catch (Exception e) {
            return ResponseEntity.status(403).body("شما مجوز ایجاد نوع حادثه را ندارید");
        }
    }

    /**
     * دریافت لیست تمام انواع حوادث موجود در سیستم
     * @param roleStr نقش کاربر که از هدر درخواست خوانده می‌شود
     * @return پاسخ HTTP شامل لیست انواع حوادث یا پیام خطا
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> getAllIncidentTypes(@RequestHeader("role") String roleStr) {
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            List<IncidentType> types = incidentTypeService.getAllIncidentTypes(role);
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(403).body("شما مجوز مشاهده لیست حوادث را ندارید");
        }
    }

    /**
     * دریافت اطلاعات یک نوع حادثه خاص بر اساس شناسه
     * @param id شناسه یکتای نوع حادثه
     * @return پاسخ HTTP شامل اطلاعات نوع حادثه یا وضعیت 404 در صورت عدم وجود
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> getIncidentTypeById(@PathVariable Long id) {
        Optional<IncidentType> type = incidentTypeService.getIncidentTypeById(id);
        return type.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * به‌روزرسانی اطلاعات یک نوع حادثه موجود
     * @param id شناسه نوع حادثه مورد نظر برای ویرایش
     * @param updatedIncidentType اطلاعات جدید برای به‌روزرسانی
     * @param roleStr نقش کاربر که از هدر درخواست خوانده می‌شود
     * @return پاسخ HTTP شامل نوع حادثه به‌روز شده یا پیام خطا
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> updateIncidentType(@PathVariable Long id,
                                                @RequestBody IncidentType updatedIncidentType,
                                                @RequestHeader("role") String roleStr) {
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            IncidentType updated = incidentTypeService.updateIncidentType(id, updatedIncidentType, role);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(403).body("شما مجوز ویرایش این نوع حادثه را ندارید");
        }
    }

    /**
     * حذف یک نوع حادثه از سیستم
     * @param id شناسه نوع حادثه مورد نظر برای حذف
     * @param roleStr نقش کاربر که از هدر درخواست خوانده می‌شود
     * @return پاسخ HTTP شامل پیام موفقیت یا خطا
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> deleteIncidentType(@PathVariable Long id,
                                                @RequestHeader("role") String roleStr) {
        try {
            Role role = Role.valueOf(roleStr.toUpperCase());
            incidentTypeService.deleteIncidentType(id, role);
            return ResponseEntity.ok("نوع حادثه با موفقیت حذف شد");
        } catch (Exception e) {
            return ResponseEntity.status(403).body("فقط مدیر سیستم می‌تواند انواع حوادث را حذف کند");
        }
    }

    /**
     * جستجوی نوع حادثه بر اساس کد یکتا
     * @param code کد یکتای نوع حادثه
     * @return پاسخ HTTP شامل اطلاعات نوع حادثه یا وضعیت 404 در صورت عدم وجود
     */
    @GetMapping("/by-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        Optional<IncidentType> type = incidentTypeService.getByCode(code);
        return type.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



}

















//package com.example.demo.controller;
//
//import com.example.demo.model.IncidentType;
//import com.example.demo.model.Role;
//import com.example.demo.service.IncidentTypeService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/incident-types")
//public class IncidentTypeController {
//
//    private final IncidentTypeService incidentTypeService;
//
//    // سازنده کلاس کنترلر که سرویس مربوط به IncidentType را تزریق می‌کند
//    public IncidentTypeController(IncidentTypeService incidentTypeService) {
//        this.incidentTypeService = incidentTypeService;
//    }
//
//    /**
//     * ایجاد نوع حادثه جدید
//     * @param incidentType داده نوع حادثه ارسالی در بدنه درخواست
//     * @param roleStr نقش کاربر که از هدر درخواست گرفته می‌شود
//     * @return پاسخ با نوع حادثه ایجاد شده یا خطا در صورت نقش نامعتبر یا دسترسی غیرمجاز
//     */
//    @PostMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> createIncidentType(@RequestBody IncidentType incidentType,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            // تبدیل رشته نقش به enum Role و بررسی صحت آن
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            // فراخوانی سرویس برای ایجاد نوع حادثه با نقش کاربر
//            IncidentType created = incidentTypeService.createIncidentType(incidentType, role);
//            // بازگرداندن پاسخ موفق با داده ایجاد شده
//            return ResponseEntity.ok(created);
//        } catch (IllegalArgumentException e) {
//            // اگر نقش ارسالی نامعتبر باشد، پاسخ 400 با پیام مناسب ارسال می‌شود
//            return ResponseEntity.status(400).body("نقش نامعتبر است");
//        } catch (Exception e) {
//            // در صورت هر خطای دیگر (مثلاً دسترسی غیرمجاز)، پاسخ 403 ارسال می‌شود
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//    }
//
//    /**
//     * دریافت همه نوع‌های حادثه
//     * @param roleStr نقش کاربر از هدر درخواست
//     * @return لیست نوع‌های حادثه یا خطای دسترسی
//     */
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> getAllIncidentTypes(@RequestHeader("role") String roleStr) {
//        try {
//            // تبدیل رشته نقش به enum و بررسی صحت آن
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            // دریافت لیست نوع‌های حادثه با توجه به نقش کاربر
//            List<IncidentType> types = incidentTypeService.getAllIncidentTypes(role);
//            // بازگرداندن لیست به صورت پاسخ موفق
//            return ResponseEntity.ok(types);
//        } catch (Exception e) {
//            // در صورت خطا یا عدم دسترسی، پاسخ 403 ارسال می‌شود
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//    }
//
//    /**
//     * دریافت نوع حادثه بر اساس شناسه
//     * @param id شناسه نوع حادثه
//     * @return نوع حادثه در صورت وجود یا پاسخ 404 در صورت عدم وجود
//     */
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> getIncidentTypeById(@PathVariable Long id) {
//        // فراخوانی سرویس برای دریافت نوع حادثه به صورت Optional
//        Optional<IncidentType> type = incidentTypeService.getIncidentTypeById(id);
//        // اگر نوع حادثه وجود داشت، با پاسخ 200 ارسال می‌شود، در غیر این صورت پاسخ 404 ارسال می‌شود
//        return type.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    /**
//     * ویرایش نوع حادثه بر اساس شناسه
//     * @param id شناسه نوع حادثه مورد نظر برای ویرایش
//     * @param updatedIncidentType داده‌های جدید برای به‌روزرسانی
//     * @param roleStr نقش کاربر از هدر درخواست
//     * @return نوع حادثه به‌روزشده یا پیام خطا در صورت عدم دسترسی یا خطا
//     */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> updateIncidentType(@PathVariable Long id,
//                                                @RequestBody IncidentType updatedIncidentType,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            // تبدیل رشته نقش به enum Role
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            // فراخوانی سرویس برای به‌روزرسانی نوع حادثه با نقش کاربر
//            IncidentType updated = incidentTypeService.updateIncidentType(id, updatedIncidentType, role);
//            // بازگرداندن نوع حادثه به‌روزشده به عنوان پاسخ موفق
//            return ResponseEntity.ok(updated);
//        } catch (Exception e) {
//            // در صورت خطا یا عدم دسترسی، پاسخ 403 با پیام خطا ارسال می‌شود
//            return ResponseEntity.status(403).body("عدم دسترسی یا خطا در ویرایش");
//        }
//    }
//
//    /**
//     * حذف نوع حادثه بر اساس شناسه
//     * @param id شناسه نوع حادثه برای حذف
//     * @param roleStr نقش کاربر از هدر درخواست
//     * @return پیام موفقیت یا خطا در صورت عدم دسترسی
//     */
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> deleteIncidentType(@PathVariable Long id,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            // تبدیل رشته نقش به enum Role
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            // فراخوانی سرویس برای حذف نوع حادثه با نقش کاربر
//            incidentTypeService.deleteIncidentType(id, role);
//            // بازگرداندن پیام موفقیت حذف
//            return ResponseEntity.ok("با موفقیت حذف شد");
//        } catch (Exception e) {
//            // در صورت عدم دسترسی یا خطا، پاسخ 403 با پیام مناسب ارسال می‌شود
//            return ResponseEntity.status(403).body("فقط مدیر اجازه حذف دارد");
//        }
//    }
//
//    /**
//     * دریافت نوع حادثه بر اساس کد
//     * @param code کد نوع حادثه
//     * @return نوع حادثه در صورت وجود یا پاسخ 404 در صورت عدم وجود
//     */
//    @GetMapping("/by-code/{code}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public ResponseEntity<?> getByCode(@PathVariable String code) {
//        // فراخوانی سرویس برای دریافت نوع حادثه بر اساس کد
//        Optional<IncidentType> type = incidentTypeService.getByCode(code);
//        // اگر وجود داشت پاسخ 200 وگرنه 404 بازگردانده می‌شود
//        return type.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//}






















//package com.example.demo.controller;
//
//import com.example.demo.model.IncidentType;
//import com.example.demo.model.Role;
//import com.example.demo.service.IncidentTypeService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/incident-types")
//public class IncidentTypeController {
//
//    private final IncidentTypeService incidentTypeService;
//
//    public IncidentTypeController(IncidentTypeService incidentTypeService) {
//        this.incidentTypeService = incidentTypeService;
//    }
//
//    // ✅ اضافه کردن نوع حادثه (همه نقش‌ها مجازند)
//    @PostMapping
//    public ResponseEntity<?> createIncidentType(@RequestBody IncidentType incidentType,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            IncidentType created = incidentTypeService.createIncidentType(incidentType, role);
//            return ResponseEntity.ok(created);
//        } catch (Exception e) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//    }
//
//    // 📋 مشاهده همه نوع‌های حادثه (فقط ADMIN و RESPONSE)
//    @GetMapping
//    public ResponseEntity<?> getAllIncidentTypes(@RequestHeader("role") String roleStr) {
//        try {
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            List<IncidentType> types = incidentTypeService.getAllIncidentTypes(role);
//            return ResponseEntity.ok(types);
//        } catch (Exception e) {
//            return ResponseEntity.status(403).body("دسترسی غیرمجاز");
//        }
//    }
//
//    // 🔍 مشاهده یک حادثه بر اساس ID (بدون محدودیت نقش)
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getIncidentTypeById(@PathVariable Long id) {
//        Optional<IncidentType> type = incidentTypeService.getIncidentTypeById(id);
//        return type.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//
//    // ✏️ ویرایش نوع حادثه (ADMIN و RESPONSE)
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateIncidentType(@PathVariable Long id,
//                                                @RequestBody IncidentType updatedIncidentType,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            IncidentType updated = incidentTypeService.updateIncidentType(id, updatedIncidentType, role);
//            return ResponseEntity.ok(updated);
//        } catch (Exception e) {
//            return ResponseEntity.status(403).body("عدم دسترسی یا خطا در ویرایش");
//        }
//    }
//
//    // ❌ حذف نوع حادثه (فقط ADMIN)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteIncidentType(@PathVariable Long id,
//                                                @RequestHeader("role") String roleStr) {
//        try {
//            Role role = Role.valueOf(roleStr.toUpperCase());
//            incidentTypeService.deleteIncidentType(id, role);
//            return ResponseEntity.ok("با موفقیت حذف شد");
//        } catch (Exception e) {
//            return ResponseEntity.status(403).body("فقط مدیر اجازه حذف دارد");
//        }
//    }
//
//    // 🔍 جستجو بر اساس کد (همه نقش‌ها مجازند)
//    @GetMapping("/by-code/{code}")
//    public ResponseEntity<?> getByCode(@PathVariable String code) {
//        Optional<IncidentType> type = incidentTypeService.getByCode(code);
//        return type.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }
//}
