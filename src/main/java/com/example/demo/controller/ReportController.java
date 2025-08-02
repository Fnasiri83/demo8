package com.example.demo.controller;

import com.example.demo.dto.FormReportDto;
import com.example.demo.dto.ReportStatisticsDTO;
import com.example.demo.model.Report;
import com.example.demo.repository.ReportRepository;
import com.example.demo.service.ReportService;
import org.springframework.core.io.Resource;

import org.springframework.core.io.UrlResource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
@Slf4j
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    private final ReportRepository reportRepository;

    public ReportController(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

@GetMapping("/test")
    public String test() {
        return "only test";
}



    // ثبت گزارش جدید بدون تصویر
    @PostMapping("/create")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Report> createReport(@RequestBody FormReportDto formReportDto) {
        Report savedReport = reportService.createReportFromDto(formReportDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
    }


        /**
         * ثبت گزارش به همراه تصویر (اختیاری)
         */

        @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @CrossOrigin(origins = "http://localhost:4200")
        public ResponseEntity<?> createReportWithImage(
                @RequestPart("formReportDto")FormReportDto formReportDto,
                @RequestPart(value = "imageFile",required = false) MultipartFile imageFile) {
            System.out.println("tttttttttttt");
            try {

                Report savedReport = reportService.createReportWithImageFromDto(formReportDto, imageFile);
                return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);

            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } catch (IOException e) {
                log.error("Error saving report image", e);
                return ResponseEntity.internalServerError().body("خطا در ذخیره تصویر گزارش");
            }
        }

        /**
         * دریافت تصویر گزارش از روی filename
         */
        @GetMapping("/images/{filename:.+}")
        public ResponseEntity<Resource> getReportImage(@PathVariable String filename) {
            try {
                Path imagePath = Paths.get("C:/temp/image").resolve(filename);
                Resource image = new UrlResource(imagePath.toUri());

                if (!image.exists() || !image.isReadable()) {
                    return ResponseEntity.notFound().build();
                }

                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(image);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        }



    /**
     * دریافت لیست تمام گزارش‌ها - فقط برای ADMIN و RESPONSE
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    /**
     * دریافت گزارش بر اساس ID - فقط برای ADMIN و RESPONSE
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(reportService.getReportById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * جستجوی گزارش‌ها بر اساس موقعیت جغرافیایی - فقط برای ADMIN و RESPONSE
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
    public ResponseEntity<List<Report>> searchByLocation(
            @RequestParam double latitude,
            @RequestParam double longitude,

            @RequestParam(defaultValue = "0.01") double radius) {
        return ResponseEntity.ok(reportService.getNearbyReports(latitude, longitude, radius));
    }

    /**
     * ویرایش گزارش - فقط برای ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Report report) {
        try {
            return ResponseEntity.ok(reportService.updateReport(id, report));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * حذف گزارش - فقط برای ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * آمار کلی گزارش‌ها - فقط برای ADMIN
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> overview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("today", reportService.countReportsToday());
        stats.put("lastWeek", reportService.countReportsLastWeek());
        stats.put("thisMonth", reportService.countReportsThisMonth());
        stats.put("byType", reportService.countReportsByIncidentType());
        stats.put("byValidity", reportService.countReportsByValidity());
        return ResponseEntity.ok(stats);
    }


    /**
     * دریافت آمار کامل گزارشات
     * @return آمار گزارشات شامل:
     * - تعداد امروز
     * - تعداد هفته جاری
     * - تعداد ماه جاری
     * - تعداد سال جاری
     * - تعداد بر اساس نوع حادثه
     * - تعداد گزارشات معتبر
     * - تعداد گزارشات جعلی
     * - آمار بر اساس نتیجه (Solved, Pending, Escalated)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ReportStatisticsDTO> getReportStatistics() {
        ReportStatisticsDTO statistics = reportService.generateReportStatistics();
        return ResponseEntity.ok(statistics);
    }

}





//                FormReportDto formReportDto = new FormReportDto();
//                formReportDto.setPhone(phone);
//                formReportDto.setIncidentType(incidentType);
//                formReportDto.setDescription(description);
//                formReportDto.setLatitude(latitude);
//                formReportDto.setLongitude(longitude);
//                formReportDto.setAddress(address);
//                formReportDto.setUsername(username);


//        @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//        @CrossOrigin(origins = "http://localhost:4200")
//        public ResponseEntity<?> createReportWithImage(
//                @RequestParam(required = false) String phone,
//                @RequestParam String incidentType,
//                @RequestParam String description,
//                @RequestParam Double latitude,
//                @RequestParam Double longitude,
//                @RequestParam(required = false) String address,
//                @RequestParam(required = false) String username,
//                @RequestPart(required = false) MultipartFile imageFile) {
//
//            System.out.println("Received file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
//
//            try {
//                FormReportDto formReportDto = new FormReportDto();
//                formReportDto.setPhone(phone);
//                formReportDto.setIncidentType(incidentType);
//                formReportDto.setDescription(description);
//                formReportDto.setLatitude(latitude);
//                formReportDto.setLongitude(longitude);
//                formReportDto.setAddress(address);
//                formReportDto.setUsername(username);
//
//                Report savedReport = reportService.createReportWithImageFromDto(formReportDto, imageFile);
//                return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
//
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("خطا در ذخیره تصویر گزارش");
//            }
//        }






//        @PostMapping("/with-image")
//        @CrossOrigin(origins = "http://localhost:4200")
//        public ResponseEntity<?> createReportWithImage(
//                @RequestParam(required = false) String phone,
//                @RequestParam String incidentType,
//                @RequestParam String description,
//                @RequestParam double latitude,
//                @RequestParam double longitude,
//                @RequestParam(required = false) String address,
//                @RequestPart(required = false) MultipartFile imageFile) {
//
//            try {
//                FormReportDto formReportDto = new FormReportDto();
//                formReportDto.setPhone(phone);
//                formReportDto.setIncidentType(incidentType);
//                formReportDto.setDescription(description);
//                formReportDto.setLatitude(latitude);
//                formReportDto.setLongitude(longitude);
//                formReportDto.setAddress(address);
//
//                Report savedReport = reportService.createReportWithImageFromDto(formReportDto, imageFile);
//                return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
//
//            } catch (IOException e) {
//                log.error("Error saving report image", e);
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("خطا در ذخیره تصویر گزارش");
//            }
//        }
//        @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//        @CrossOrigin(origins = "http://localhost:4200")
//        public ResponseEntity<?> createReportWithImage(
//                @RequestPart(required = false) String phone,
//                @RequestPart String incidentType,
//                @RequestPart String description,
//                @RequestPart Double latitude,
//                @RequestPart Double longitude,
//                @RequestPart(required = false) String address,
//                @RequestPart(required = false) MultipartFile imageFile,
//                @RequestPart(required = false) String username) {
//            System.out.println("Received file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
//
//
//            try {
//                FormReportDto formReportDto = new FormReportDto();
//                formReportDto.setPhone(phone);
//                formReportDto.setIncidentType(incidentType);
//                formReportDto.setDescription(description);
//                formReportDto.setLatitude(latitude);
//                formReportDto.setLongitude(longitude);
//                formReportDto.setAddress(address);
//                formReportDto.setUsername(username);
//
//                Report savedReport = reportService.createReportWithImageFromDto(formReportDto, imageFile);
//                return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
//
//            } catch (IOException e) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("خطا در ذخیره تصویر گزارش");
//
//            }
//        }




//package com.example.demo.controller;
//
//import com.example.demo.model.Report;
//i
//
//
////    @PostMapping("/with-image")
////    public ResponseEntity<?> createReportWithImage( @ModelAttribute FormReportDto formReportDto,
////            @RequestParam(required = false) String phone,
////                                                   @RequestParam(required = false) MultipartFile image,
////                                                   @RequestParam String incidentType,
////                                                   @RequestParam String description,
////                                                  @RequestParam double latitude,
////                                                   @RequestParam double longitude,
////                                                    @RequestParam(required = false)String address,
////                                                   @RequestPart(required = false) MultipartFile imageFile
////    ) {
////
////        try {
////            Report savedReport = reportService.createReportWithImageFromDto(formReportDto, imageFile);
////            return ResponseEntity.status(HttpStatus.CREATED).body(savedReport);
////        } catch (IOException e) {
////            log.error("Error saving report image", e);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
////                    .body("خطا در ذخیره تصویر گزارش");
////        }
////    }
////    @PostMapping("/with-image")
////    public ResponseEntity<?> createReportWithImage( @RequestParam(required = false) String phone,
////                                                   @RequestParam String incidentType,
////                                                   @RequestParam String description,
////                                                   @RequestParam double latitude,
////                                                   @RequestParam double longitude,
////                                                    @RequestParam(required = false)String address,
////                                                   @RequestPart(required = false) MultipartFile imageFile) {
////        try {
////            Report report = reportService.createReportWithImage(phone, incidentType, description, latitude, longitude, imageFile);
////            return ResponseEntity.ok(report);
////        } catch (IOException e) {
////            return ResponseEntity.status(500).body("خطا در ذخیره تصویر");
////        } catch (RuntimeException e) {
////            return ResponseEntity.badRequest().body(e.getMessage());
////        }
////    }mport com.example.demo.service.ReportService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reports")
//public class ReportController {
//
//    private final ReportService reportService;
//
//    public ReportController(ReportService reportService) {
//        this.reportService = reportService;
//    }
//
//    // ✅ ثبت گزارش جدید بدون تصویر
//    @PostMapping("/create")
//    public ResponseEntity<?> createReport(@RequestBody Report report,
//                                          @RequestParam String phone) {
//        try {
//            Report savedReport = reportService.createReport(report, phone);
//            return ResponseEntity.ok(savedReport);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    // 🖼 ثبت گزارش با تصویر
//    @PostMapping("/with-image")
//    public ResponseEntity<?> createReportWithImage(@RequestParam String phone,
//                                                   @RequestParam String incidentType,
//                                                   @RequestParam String description,
//                                                   @RequestParam double latitude,
//                                                   @RequestParam double longitude,
//                                                   @RequestPart(required = false) MultipartFile imageFile) {
//        try {
//            Report report = reportService.createReportWithImage(phone, incidentType, description, latitude, longitude, imageFile);
//            return ResponseEntity.ok(report);
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("خطا در ذخیره تصویر");
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    // 📋 دریافت همه گزارش‌ها
//    @GetMapping
//    public ResponseEntity<List<Report>> getAllReports() {
//        return ResponseEntity.ok(reportService.getAllReports());
//    }
//
//    // 🔍 دریافت گزارش بر اساس ID
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getReportById(@PathVariable Long id) {
//        try {
//            Report report = reportService.getReportById(id);
//            return ResponseEntity.ok(report);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // 🔍 جستجوی گزارش‌ها با کلمه کلیدی موقعیت
//    @GetMapping("/search")
//    public ResponseEntity<List<Report>> searchReportsByLocation(@RequestParam String keyword) {
//        return ResponseEntity.ok(reportService.getNearbyReports(keyword));
//}
//}
//    public ResponseEntity<Report> createReport(@RequestBody FormReportDto formReportDto,
//                                          @RequestParam(required = false) String phone) {
//        try {
//            Report savedReport = reportService.createReport(report, phone);
//            return ResponseEntity.ok(savedReport);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }