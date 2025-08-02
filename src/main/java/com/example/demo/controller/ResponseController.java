package com.example.demo.controller;

import com.example.demo.model.Report;
import com.example.demo.model.Response;
import com.example.demo.repository.ReportRepository;
import com.example.demo.service.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * کنترلر جامع مدیریت پاسخ‌ها به گزارشات
 * این کنترلر تمام عملیات CRUD مربوط به پاسخ‌ها و همچنین عملیات تخصصی مانند:
 * - ثبت پاسخ به گزارشات
 * - مدیریت وضعیت مشاهده
 * - جستجوی پاسخ‌ها بر اساس موقعیت جغرافیایی
 * را ارائه می‌دهد.
 */
@RestController
@RequestMapping("/api/responses")
public class ResponseController {

    private final ResponseService responseService;
    private final ReportRepository reportRepository;

    /**
     * سازنده کنترلر با تزریق وابستگی‌های لازم
     * @param responseService سرویس مدیریت پاسخ‌ها
     * @param reportRepository ریپازیتوری دسترسی به گزارشات
     */
    public ResponseController(ResponseService responseService, ReportRepository reportRepository) {
        this.responseService = responseService;
        this.reportRepository = reportRepository;
    }

    /**
     * ایجاد پاسخ جدید برای یک گزارش
     * @param reportId شناسه گزارش مربوطه
     * @param content محتوای پاسخ
     * @param responder نام پاسخ‌دهنده
     * @return پاسخ ایجاد شده با کد وضعیت 201
     */
    @PostMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<Response> createResponse(
            @PathVariable Long reportId,
            @RequestParam String content,
            @RequestParam String responder) {

        Response response = responseService.createResponse(reportId, content, responder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ثبت پاسخ نهایی برای یک گزارش (شامل نتیجه بررسی)
     * @param response اطلاعات کامل پاسخ
     * @return پاسخ ثبت شده یا پیام خطا در صورت وجود مشکل
     */
    @PostMapping("/respond")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> respondToReport(@RequestBody Response response) {
        try {
            Response savedResponse = responseService.respondToReport(response);
            return ResponseEntity.ok(savedResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * دریافت تمام پاسخ‌های سیستم
     * @return لیست تمام پاسخ‌ها
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<List<Response>> getAllResponses() {
        List<Response> responses = responseService.getAllResponses();
        return ResponseEntity.ok(responses);
    }

    /**
     * دریافت پاسخ‌های مربوط به یک گزارش خاص
     * @param reportId شناسه گزارش مورد نظر
     * @return لیست پاسخ‌های مرتبط با گزارش
     */
    @GetMapping("/report/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<List<Response>> getResponsesByReportId(@PathVariable Long reportId) {
        List<Response> responses = responseService.getResponsesByReportId(reportId);
        return ResponseEntity.ok(responses);
    }

    /**
     * جستجوی پاسخ‌ها بر اساس موقعیت جغرافیایی گزارش مرتبط
     * @param latitude عرض جغرافیایی مرکز جستجو
     * @param longitude طول جغرافیایی مرکز جستجو
     * @param radius شعاع جستجو بر حسب درجه (پیش‌فرض: 0.01)
     * @return لیست پاسخ‌های موجود در محدوده مشخص شده
     */
    @GetMapping("/search/by-location")
    public ResponseEntity<List<Response>> searchByCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "0.01") double radius) {

        List<Response> filtered = responseService.getAllResponses().stream()
                .filter(r -> {
                    Report report = r.getReport();
                    if (report == null) return false;

                    double latDiff = Math.abs(report.getLatitude() - latitude);
                    double lonDiff = Math.abs(report.getLongitude() - longitude);

                    return latDiff <= radius && lonDiff <= radius;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /**
     * ثبت وضعیت مشاهده برای یک پاسخ
     * @param responseId شناسه پاسخ
     * @param response اطلاعات به‌روزرسانی شده پاسخ
     * @return پاسخ با وضعیت مشاهده به‌روزرسانی شده
     */
    @PutMapping("/{responseId}/seen")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<Response> markAsSeen(
            @PathVariable Long responseId,
            @RequestBody Response response) {

        response.setSeen(true);
        response.setSeenAt(LocalDateTime.now());
        Response updatedResponse = responseService.saveSeenResponse(response);
        return ResponseEntity.ok(updatedResponse);
    }

    /**
     * ثبت وضعیت مشاهده برای یک گزارش (رکورد جدید پاسخ ایجاد می‌کند)
     * @param reportId شناسه گزارش
     * @return پاسخ جدید با وضعیت مشاهده شده
     */
    @PostMapping("/seen/{reportId}")
    @PreAuthorize("hasRole('RESPONSE')")
    public ResponseEntity<?> markReportAsSeen(@PathVariable Long reportId) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }

        Response response = new Response();
        response.setSeen(true);
        response.setSeenAt(LocalDateTime.now());
        response.setReport(report);

        Response saved = responseService.saveSeenResponse(response);
        return ResponseEntity.ok(saved);
    }

    /**
     * حذف یک پاسخ
     * @param responseId شناسه پاسخ
     * @return پاسخ بدون محتوا در صورت موفقیت (کد 204)
     */
    @DeleteMapping("/{responseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteResponse(@PathVariable Long responseId) {
        responseService.deleteResponse(responseId);
        return ResponseEntity.noContent().build();
    }
}






























//package com.example.demo.controller;
//
//import com.example.demo.model.Report;
//import com.example.demo.model.Response;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.service.ResponseService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/responses")
//public class ResponseController {
//
//    private final ResponseService responseService;
//    private final ReportRepository reportRepository;
//
//    public ResponseController(ResponseService responseService, ReportRepository reportRepository) {
//        this.responseService = responseService;
//        this.reportRepository = reportRepository;
//    }
//
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<Response>> getAllResponses() {
//        return ResponseEntity.ok(responseService.getAllResponses());
//    }
//
//    @GetMapping("/search/by-location")
//    public ResponseEntity<List<Response>> searchByCoordinates(
//            @RequestParam double latitude,
//            @RequestParam double longitude,
//            @RequestParam(required = false, defaultValue = "0.01") double radius
//    ) {
//        List<Response> allResponses = responseService.getAllResponses();
//
//        List<Response> filtered = allResponses.stream()
//                .filter(r -> {
//                    Report report = r.getReport();
//                    if (report == null) return false;
//
//                    double lat = report.getLatitude();
//                    double lon = report.getLongitude();
//
//                    double latDiff = Math.abs(lat - latitude);
//                    double lonDiff = Math.abs(lon - longitude);
//
//                    return latDiff <= radius && lonDiff <= radius;
//                })
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(filtered);
//    }
//
//    @PostMapping
//    @PreAuthorize("hasRole('RESPONSE && ADMIN')")
//    public ResponseEntity<?> respondToReport(@RequestBody Response response) {
//        try {
//            Response savedResponse = responseService.respondToReport(response);
//            return ResponseEntity.ok(savedResponse);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PostMapping("/seen/{reportId}")
//    @PreAuthorize("hasRole('RESPONSE')")
//    public ResponseEntity<?> markReportAsSeen(@PathVariable Long reportId) {
//        Report report = reportRepository.findById(reportId).orElse(null);
//        if (report == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Response response = new Response();
//        response.setSeen(true);
//        response.setSeenAt(LocalDateTime.now());
//        response.setReport(report);
//
//        Response saved = responseService.saveSeenResponse(response);
//        return ResponseEntity.ok(saved);
//    }
//}
//
//














//package com.example.demo.controller;
//
//import com.example.demo.model.Report;
//import com.example.demo.model.Response;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.service.ResponseService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/responses")
//public class ResponseController {
//
//    private final ResponseService responseService;
//    private final ReportRepository reportRepository;
//
//    public ResponseController(ResponseService responseService, ReportRepository reportRepository) {
//        this.responseService = responseService;
//        this.reportRepository = reportRepository;
//    }
//
//    // 🧾 فقط ادمین‌ها می‌تونن تمام پاسخ‌ها رو ببینن
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<Response>> getAllResponses() {
//        return ResponseEntity.ok(responseService.getAllResponses());
//    }
//
//    @GetMapping("/search/by-location")
//    public ResponseEntity<List<Response>> searchByCoordinates(
//            @RequestParam double latitude,
//            @RequestParam double longitude,
//            @RequestParam(required = false, defaultValue = "0.01") double radius
//    ) {
//        // تمام پاسخ‌ها
//        List<Response> allResponses = responseService.getAllResponses();
//
//        // فیلتر کردن گزارش‌هایی که مختصات دارند و داخل شعاع مشخص‌شده هستند
//        List<Response> filtered = allResponses.stream()
//                .filter(r -> {
//                    Report report = r.getReport();
//                    if (report == null) return false;
//
//                    double lat = report.getLatitude();
//                    double lon = report.getLongitude();
//
//                    // اختلاف ساده بین دو نقطه (برای محدوده تقریبی، نه دقیق علمی)
//                    double latDiff = Math.abs(lat - latitude);
//                    double lonDiff = Math.abs(lon - longitude);
//
//                    return latDiff <= radius && lonDiff <= radius;
//                })
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(filtered);
//    }
//
//    // ✅ پاسخ به گزارش (فقط توسط کاربران با نقش RESPONSE - امدادگران)
//    @PostMapping
//    @PreAuthorize("hasRole('RESPONSE')")
//    public ResponseEntity<?> respondToReport(@RequestBody Response response) {
//        try {
//            Response savedResponse = responseService.respondToReport(response);
//            return ResponseEntity.ok(savedResponse);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    // 👁‍🗨 علامت‌گذاری گزارش به عنوان دیده‌شده (فقط امدادگران)
//    @PostMapping("/seen/{reportId}")
//    @PreAuthorize("hasRole('RESPONSE')")
//    public ResponseEntity<?> markReportAsSeen(@PathVariable Long reportId) {
//        Report report = reportRepository.findById(reportId).orElse(null);
//        if (report == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Response response = new Response();
//        response.setSeen(true);
//        response.setSeenAt(LocalDateTime.now());
//        response.setReport(report);
//
//        return ResponseEntity.ok(response);
//    }
//}
