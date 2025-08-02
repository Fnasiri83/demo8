package com.example.demo.service;

import com.example.demo.dto.FormReportDto;
import com.example.demo.dto.ReportStatisticsDTO;
import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import com.example.demo.repository.IncidentTypeRepository;
import com.example.demo.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j // یا تعریف دستی logger

@Service
public class ReportService {
    @Value("${app.report.image.storage-path}")
    private String reportImageStoragePath;
@Autowired
    private final ReportRepository reportRepository;
    private final IncidentTypeService incidentTypeService;
    private final IncidentTypeRepository incidentTypeRepository;
    private final Path root = Paths.get("uploads");

    public ReportService(ReportRepository reportRepository, IncidentTypeService incidentTypeService, IncidentTypeRepository incidentTypeRepository) {
        this.reportRepository = reportRepository;
        this.incidentTypeService = incidentTypeService;
        this.incidentTypeRepository = incidentTypeRepository;

        try {
            Files.createDirectories(root); // ایجاد پوشه اگر وجود نداشت
        } catch (IOException e) {
            throw new RuntimeException("خطا در ایجاد پوشه آپلود", e);
        }
    }

    // ذخیره گزارش بدون عکس
    public Report createReport(Report report, String phone) {
        report.setPhone(phone);
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }


    /**
     * تبدیل FormReportDto به موجودیت Report
     */
    private Report convertToReport(FormReportDto formReportDto) {
        Report report = new Report();
        report.setDescription(formReportDto.getDescription());
        report.setIncidentType(formReportDto.getIncidentType());
        report.setAddress(formReportDto.getAddress());
        report.setLongitude(formReportDto.getLongitude());
        report.setLatitude(formReportDto.getLatitude());
        report.setPhone(formReportDto.getPhone());

        // مقادیر پیش‌فرض
        report.setCreatedAt(LocalDateTime.now());
        report.setStatus(ReportStatus.UNSEEN);
        report.setValid(false);
        report.setProcessed(false);
        report.setFake(false);

        return report;
    }

    /**
     * ایجاد گزارش جدید از FormReportDto (بدون تصویر)
     */
    @Transactional
    public Report createReportFromDto(FormReportDto formReportDto) {
        Report report = convertToReport(formReportDto);
        return reportRepository.save(report);
    }

    /**
     * ایجاد گزارش با تصویر از FormReportDto
     */
    @Transactional
    public Report createReportWithImageFromDto(FormReportDto formReportDto, MultipartFile imageFile) throws IOException {
        Report report = convertToReport(formReportDto);

        if (imageFile != null && !imageFile.isEmpty()) {
            // بررسی نوع فایل
            String contentType = imageFile.getContentType();
            if (!isValidImageType(contentType)) {
                throw new IllegalArgumentException("فرمت تصویر نامعتبر است");
            }

            // مسیر ذخیره‌سازی ثابت (همانند getReportImage)
            String uploadDir = "C:/temp/image"; // یا بخوان از properties با @Value

            // نام فایل منحصر به‌فرد
            String fileExtension = getFileExtension(imageFile.getOriginalFilename());
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            Path filePath = Paths.get(uploadDir).resolve(uniqueFilename);

            // ایجاد پوشه در صورت نیاز
            Files.createDirectories(Paths.get(uploadDir));

            // ذخیره فایل
            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            // مسیر نسبی جهت دسترسی از API
            report.setImagePath("/api/reports/images/" + uniqueFilename);
        }

        return reportRepository.save(report);
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif"));
    }

    private String getFileExtension(String filename) {
        return filename != null ? filename.substring(filename.lastIndexOf(".")) : "";
    }






    // ذخیره گزارش با عکس
//    public Report createReportWithImage(String phone, String incidentType, String description,
//                                        double latitude, double longitude, MultipartFile imageFile) throws IOException {
//        Report report = new Report();
//        report.setPhone(phone);
//        report.setIncidentType(incidentType);
//        report.setDescription(description);
//        report.setLatitude(latitude);
//        report.setLongitude(longitude);
//        report.setCreatedAt(LocalDateTime.now());
//
//        // ذخیره عکس در صورت وجود
//        if (imageFile != null && !imageFile.isEmpty()) {
//            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
//            String uploadDir = "uploads/report_images/";
//            File dir = new File(uploadDir);
//            if (!dir.exists()) dir.mkdirs();
//
//            String filePath = uploadDir + fileName;
//            Files.write(Paths.get(filePath), imageFile.getBytes());
//            report.setImagePath(filePath);
//        }
//
//        return reportRepository.save(report);
//    }


//   @Transactional
//public Report createReportWithImageFromDto(FormReportDto formReportDto, MultipartFile imageFile) throws IOException {
//    Report report = convertToReport(formReportDto);
//
//    if (imageFile != null && !imageFile.isEmpty()) {
//        String uploadDir = "C:/temp/image"; // مسیر ذخیره‌سازی اصلاح شده
//        String originalFilename = imageFile.getOriginalFilename();
//        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
//        String filePath = uploadDir + File.separator + uniqueFilename;
//
//        File dir = new File(uploadDir);
//        try {
//            if (!dir.exists()) {
//                boolean created = dir.mkdirs();
//                System.out.println("Directory created: " + created + " at " + dir.getAbsolutePath());
//            }
//
//            File convertFile = new File(filePath);
//            if (convertFile.createNewFile()) {
//                System.out.println("File created: " + convertFile.getAbsolutePath());
//            } else {
//                System.out.println("File already exists: " + convertFile.getAbsolutePath());
//            }
//
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(imageFile.getBytes());
//                System.out.println("Image written to file successfully.");
//            }
//        } catch (IOException e) {
//            System.err.println("Error saving image file: " + e.getMessage());
//            throw e;
//        }
//
//        // تنظیم مسیر دسترسی به تصویر برای کلاینت
//        report.setImagePath("/api/reports/images/" + uniqueFilename);
//    }
//
//    return reportRepository.save(report);
//}


    // دریافت همه گزارش‌ها
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // دریافت گزارش بر اساس آیدی
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("گزارش پیدا نشد"));
    }

    // جستجو بر اساس موقعیت جغرافیایی
    public List<Report> getNearbyReports(double latitude, double longitude, double radius) {
        return reportRepository.findAll().stream()
                .filter(r -> Math.abs(r.getLatitude() - latitude) <= radius &&
                        Math.abs(r.getLongitude() - longitude) <= radius)
                .collect(Collectors.toList());
    }

    // بروزرسانی گزارش
    public Report updateReport(Long id, Report updated) {
        Report report = getReportById(id);
        report.setIncidentType(updated.getIncidentType());
        report.setDescription(updated.getDescription());
        report.setLatitude(updated.getLatitude());
        report.setLongitude(updated.getLongitude());
        report.setValid(updated.getValid());
        report.setProcessed(updated.getProcessed());
        return reportRepository.save(report);
    }

    // حذف گزارش
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    // آمار گزارش‌ها در روز جاری
    public long countReportsToday() {
        return reportRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
    }

    // آمار گزارش‌ها در هفته اخیر
    public long countReportsLastWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        return reportRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(weekAgo))
                .count();
    }

    // آمار گزارش‌ها در ماه جاری
    public long countReportsThisMonth() {
        int month = LocalDateTime.now().getMonthValue();
        return reportRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().getMonthValue() == month)
                .count();
    }

    // آمار بر اساس نوع حادثه
    public Map<String, Long> countReportsByIncidentType() {
        return reportRepository.findAll().stream()
                .collect(Collectors.groupingBy(Report::getIncidentType, Collectors.counting()));
    }

    // آمار بر اساس اعتبار گزارش
    public Map<String, Long> countReportsByValidity() {
        return reportRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getValid() ? "معتبر" : "نامعتبر", Collectors.counting()));
    }
    // پردازش توصیفی گزارش (دمو برای NLP یا تحلیل‌های دیگر)
    public String processContent(String type, String desc) {
        return "تحلیل نوع [" + type + "] و شرح [" + desc + "] انجام شد.";
    }
    public ReportStatisticsDTO generateReportStatistics() {
        try {
            ReportStatisticsDTO stats = new ReportStatisticsDTO();

            // محاسبه آمار زمانی
            LocalDateTime now = LocalDateTime.now();
            stats.setTodayCount(reportRepository.countReportsToday());
            stats.setWeekCount(reportRepository.countReportsThisWeek(now.minusWeeks(1)));
            stats.setMonthCount(reportRepository.countReportsThisMonth());
            stats.setYearCount(reportRepository.countReportsThisYear());

            // آمار بر اساس نوع حادثه
            Map<String, Long> incidentStats = reportRepository.countReportsByIncidentType();
            stats.setCountByIncidentType(incidentStats != null ? incidentStats : new HashMap<>());

            // آمار اعتبار گزارشات
            stats.setValidReports(reportRepository.countByIsFakeFalse());
            stats.setFakeReports(reportRepository.countByIsFakeTrue());

            // آمار بر اساس وضعیت (با توجه به enum ReportStatus)
            Map<String, Long> statusStats = new HashMap<>();
            statusStats.put("SEEN", reportRepository.countByStatus(ReportStatus.SEEN));
            statusStats.put("UNSEEN", reportRepository.countByStatus(ReportStatus.UNSEEN));
            statusStats.put("HANDLED", reportRepository.countByStatus(ReportStatus.HANDLED));
            statusStats.put("FAKE", reportRepository.countByStatus(ReportStatus.FAKE));

            stats.setOutcomeStats(statusStats);

            return stats;
        } catch (Exception e) {
            log.error("Error generating report statistics", e);
            return new ReportStatisticsDTO();
        }
    }

}




//@Transactional
//    public Report createReportWithImageFromDto(FormReportDto formReportDto, MultipartFile imageFile) throws IOException {
//        Report report = convertToReport(formReportDto);
//
//        if (imageFile != null && !imageFile.isEmpty()) {
//            String uploadDir = "C:/temp/image"; // مسیر ذخیره‌سازی اصلاح شده
//            String originalFilename = imageFile.getOriginalFilename();
//            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;
//            String filePath = uploadDir + File.separator + uniqueFilename;
//
//            File dir = new File(uploadDir);
//            try {
//                if (!dir.exists()) {
//                    boolean created = dir.mkdirs();
//                    System.out.println("Directory created: " + created + " at " + dir.getAbsolutePath());
//                }
//
//                File convertFile = new File(filePath);
//                if (convertFile.createNewFile()) {
//                    System.out.println("File created: " + convertFile.getAbsolutePath());
//                } else {
//                    System.out.println("File already exists: " + convertFile.getAbsolutePath());
//                }
//
//                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                    fos.write(imageFile.getBytes());
//                    System.out.println("Image written to file successfully.");
//                }
//            } catch (IOException e) {
//                System.err.println("Error saving image file: " + e.getMessage());
//                throw e;
//            }
//
//            // تنظیم مسیر دسترسی به تصویر برای کلاینت
//            report.setImagePath("/api/reports/images/" + uniqueFilename);
//        }
//
//        return reportRepository.save(report);
//    }









//package com.example.demo.service;
//
//import com.example.demo.dto.ReportStatisticsDTO;
//import com.example.demo.model.IncidentType;
//import com.example.demo.model.Report;
//import com.example.demo.model.ReportStatus;
//import com.example.demo.model.User;
//import com.example.demo.repository.IncidentTypeRepository;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//public class ReportService {
//
//    private final ReportRepository reportRepository;
//    private final UserRepository userRepository;
//    private final IncidentTypeRepository incidentTypeRepository;
//@Autowired
//    public ReportService(ReportRepository reportRepository, UserRepository userRepository, IncidentTypeRepository incidentTypeRepository) {
//        this.reportRepository = reportRepository;
//        this.userRepository = userRepository;
//        this.incidentTypeRepository = incidentTypeRepository;
//    }
//
//    // ثبت گزارش جدید به همراه کاربر گزارش‌دهنده (reporter)
//    public Report createReport(Report report, String reporterPhone) {
//        User reporter = userRepository.findByPhone(reporterPhone)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        report.setReporter(reporter);
//        report.setStatus(ReportStatus.UNSEEN);
//        return reportRepository.save(report);
//    }
//
//
//
//    public Report createReportWithImage(String phone, String incidentTypeStr, String description,
//                                        double latitude, double longitude, MultipartFile imageFile) throws IOException {
//        User reporter = userRepository.findByPhone(phone)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // اصلاح متد به findByName چون ریپازیتوری findByTitle نداره
//        IncidentType incidentType = incidentTypeRepository.findByTitle(incidentTypeStr)
//                .orElseThrow(() -> new RuntimeException("IncidentType not found"));
//
//        Report report = new Report();
//        report.setIncidentType(incidentType);
//        report.setDescription(description);
//        report.setLatitude(latitude);
//        report.setLongitude(longitude);
//        report.setReporter(reporter);
//        report.setStatus(ReportStatus.UNSEEN);
//
//        return reportRepository.save(report);
//    }
//
//
//    // دریافت گزارش بر اساس آیدی
//    public Report getReportById(Long reportId) {
//        return reportRepository.findById(reportId)
//                .orElseThrow(() -> new RuntimeException("Report not found"));
//    }
//
//    // دریافت تمامی گزارش‌ها
//    public List<Report> getAllReports() {
//        return reportRepository.findAll();
//    }
//
//    // بروزرسانی اطلاعات یک گزارش
//    public Report updateReport(Long id, Report updatedReport) {
//        Report report = reportRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Report not found"));
//
//        // بروزرسانی گزارش با اطلاعات جدید
//        report.setIncidentType(updatedReport.getIncidentType());
//        report.setDescription(updatedReport.getDescription());
//        report.setLatitude(updatedReport.getLatitude());
//        report.setLongitude(updatedReport.getLongitude());
//        report.setStatus(updatedReport.getStatus());
//        report.setReporter(updatedReport.getReporter());
//        return reportRepository.save(report);
//    }
//
//    // حذف گزارش بر اساس ID
//    public void deleteReport(Long id) {
//        reportRepository.deleteById(id);
//    }
//
//    // بخش getNearbyReports درست شد و findByTitle هم اصلاح شد
//    public List<Report> getNearbyReports(double latitude, double longitude, double radius) {
//        List<Report> allReports = reportRepository.findAll();
//
//        return allReports.stream()
//                .filter(report -> {
//                    double latDiff = Math.abs(report.getLatitude() - latitude);
//                    double lonDiff = Math.abs(report.getLongitude() - longitude);
//                    return latDiff <= radius && lonDiff <= radius;
//                })
//                .collect(Collectors.toList());
//    }
//
//    public Long countReportsToday() {
//        return reportRepository.countReportsToday();
//    }
//
//    public Long countReportsLastWeek() {
//        LocalDate lastWeek = LocalDate.now().minusDays(7);
//        return reportRepository.countReportsLastWeek(lastWeek);
//    }
//
//
//    public Long countReportsThisMonth() {
//        return reportRepository.countReportsThisMonth();
//    }
//
//    public Map<String, Long> countReportsByIncidentType() {
//        List<Object[]> results = reportRepository.countReportsByIncidentType();
//        Map<String, Long> map = new HashMap<>();
//        for (Object[] obj : results) {
//            map.put((String) obj[0], (Long) obj[1]);
//        }
//        return map;
//    }
//
//    public Map<String, Long> countReportsByValidity() {
//        List<Object[]> results = reportRepository.countReportsByValidity();
//        Map<String, Long> map = new HashMap<>();
//        for (Object[] obj : results) {
//            Boolean isValid = (Boolean) obj[0];
//            Long count = (Long) obj[1];
//            map.put(isValid == null ? "نامشخص" : (isValid ? "درست" : "دروغ"), count);
//        }
//        return map;
//    }
//
//
//    public Report uploadReportImage(Long reportId, MultipartFile file) throws IOException {
//        Report report = reportRepository.findById(reportId)
//                .orElseThrow(() -> new RuntimeException("گزارش پیدا نشد"));
//
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//        String uploadDir = "uploads/report_images/";
//
//        File dir = new File(uploadDir);
//        if (!dir.exists()) dir.mkdirs();
//
//        Path path = Paths.get(uploadDir + fileName);
//        Files.write(path, file.getBytes());
//
//        report.setImagePath(path.toString());
//        return reportRepository.save(report);
//
//    }
//
//    public String processContent(String description, String location) {
//        switch (location.toLowerCase()) {
//            case "description":
//                return "توضیح حادثه دریافت شد: " ;
//            case "latitude":
//                return "عرض جغرافیایی دریافت شد و ذخیره شد.";
//            case "longitude":
//                return "طول جغرافیایی دریافت شد و ذخیره شد.";
//            default:
//                return "نوع محتوا نامعتبر است.";
//        }
//    }
//    // محاسبه تعداد گزارش‌ها بعد از یک هفته گذشته
//    public Long countReportsAfterLastWeek() {
//        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
//        return reportRepository.countReportsAfter(lastWeek);
//    }
//
//}




































//package com.example.demo.service;
//
//import com.example.demo.model.IncidentType;
//import com.example.demo.model.Report;
//import com.example.demo.model.ReportStatus;
//import com.example.demo.model.User;
//import com.example.demo.repository.IncidentTypeRepository;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.repository.UserRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class ReportService {
//
//    private final ReportRepository reportRepository;
//    private final UserRepository userRepository;
//    private final IncidentTypeRepository incidentTypeRepository;
//
//    public ReportService(ReportRepository reportRepository, UserRepository userRepository, IncidentTypeRepository incidentTypeRepository) {
//        this.reportRepository = reportRepository;
//        this.userRepository = userRepository;
//        this.incidentTypeRepository = incidentTypeRepository;
//    }
//
//    // ثبت گزارش جدید به همراه کاربر گزارش‌دهنده (reporter)
//    public Report createReport(Report report, String reporterPhone) {
//        // پیدا کردن کاربر بر اساس شماره موبایل
//        User reporter = userRepository.findByPhone(reporterPhone)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        report.setReporter(reporter);
//        report.setStatus(ReportStatus.UNSEEN); // یا هر وضعیت اولیه که مد نظر دارید، برای مثال UNSEEN
//        return reportRepository.save(report);
//    }
//
//    // دریافت گزارش‌ها بر اساس کلمه کلیدی (برای موقعیت یا هر فیلد متنی)
//    public List<Report> getNearbyReports(String locationKeyword) {
//        return reportRepository.findByLocationContaining(locationKeyword);
//    }
//
//    // دریافت گزارش بر اساس آیدی
//    public Report getReportById(Long reportId) {
//        return reportRepository.findById(reportId)
//                .orElseThrow(() -> new RuntimeException("Report not found"));
//    }
//
//    // دریافت تمامی گزارش‌ها
//    public List<Report> getAllReports() {
//        return reportRepository.findAll();
//    }
//
//    public Report createReportWithImage(String phone, String incidentTypeStr, String description,
//                                        double latitude, double longitude, MultipartFile imageFile) throws IOException {
//        // پیدا کردن کاربر بر اساس شماره تلفن
//        User reporter = userRepository.findByPhone(phone)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // پیدا کردن شیء IncidentType بر اساس رشته دریافتی
//        IncidentType incidentType = incidentTypeRepository.findByName(incidentTypeStr)
//                .orElseThrow(() -> new RuntimeException("IncidentType not found"));
//
//        // ساخت گزارش
//        Report report = new Report();
//        report.setIncidentType(incidentType); // حالا شیء صحیح ارسال میشه
//        report.setDescription(description);
//        report.setLatitude(latitude);
//        report.setLongitude(longitude);
//        report.setReporter(reporter);
//        report.setStatus(ReportStatus.UNSEEN);
//        // ادامه کد مربوط به ذخیره فایل تصویر و ثبت گزارش در دیتابیس ...
//
//        return reportRepository.save(report);
//    }
//
//}





















//        // ایجاد گزارش جدید
//        Report report = new Report();
//        report.setReporter(reporter);
//        report.setIncidentType(incidentType); // تنظیم نوع حادثه
//        report.setDescription(description); // توضیحات
//        report.setLatitude(latitude);
//        report.setLongitude(longitude);
//        report.setStatus(ReportStatus.PENDING); // وضعیت اولیه
//        report.setCreatedAt(LocalDateTime.now()); // زمان ایجاد گزارش
//
//        // ذخیره تصویر در سیستم فایل (مسیر تصویر را در گزارش ذخیره می‌کنیم)
//        if (imageFile != null && !imageFile.isEmpty()) {
//            // مسیر ذخیره‌سازی فایل
//            String imagePath = "/path/to/save/images/" + imageFile.getOriginalFilename();
//            File destinationFile = new File(imagePath);
//            imageFile.transferTo(destinationFile);
//            report.setImagePath(imagePath); // تنظیم مسیر فایل در گزارش
//        }
//
//        // ذخیره گزارش در دیتابیس
//        return reportRepository.save(report);
//    }

