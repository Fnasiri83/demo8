package com.example.demo.service;

import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import com.example.demo.model.Response;
import com.example.demo.repository.ReportRepository;
import com.example.demo.repository.ResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final ReportRepository reportRepository;

    public ResponseService(ResponseRepository responseRepository, ReportRepository reportRepository) {
        this.responseRepository = responseRepository;
        this.reportRepository = reportRepository;
    }

    /**
     * ایجاد پاسخ جدید برای گزارش
     *
     * @param reportId  شناسه گزارش
     * @param content   محتوای پاسخ
     * @param responder نام پاسخ‌دهنده
     * @return پاسخ ایجاد شده
     * @throws RuntimeException اگر گزارش یافت نشود
     */
    @Transactional
    public Response createResponse(final Long reportId, final String content, final String responder) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("گزارش مورد نظر یافت نشد"));

        Response response = new Response();
        response.setReport(report);
        response.setContent(content);
        response.setResponder(responder);
        response.setRole("RESPONSE"); // تنظیم نقش پاسخ‌دهنده
        response.setCreatedAt(LocalDateTime.now());
        response.setResponseTime(LocalDateTime.now());

        return responseRepository.save(response);
    }

    /**
     * پاسخگویی به گزارش با مدیریت وضعیت
     *
     * @param response اطلاعات پاسخ
     * @return پاسخ ذخیره شده
     * @throws IllegalStateException اگر گزارش مشاهده نشده باشد
     */
    @Transactional
    public Response respondToReport(final Response response) {
        Report report = response.getReport();

        if (!response.isSeen()) {
            report.setStatus(ReportStatus.UNSEEN);
            reportRepository.save(report);
            throw new IllegalStateException("امدادگر هنوز گزارش را مشاهده نکرده است");
        }

        response.setResponseTime(LocalDateTime.now());

        if (response.getSeenAt() == null) {
            response.setSeenAt(LocalDateTime.now());
        }

        // بروزرسانی وضعیت گزارش بر اساس نتیجه
        if ("FAKE".equalsIgnoreCase(response.getResult())) {
            report.setStatus(ReportStatus.FAKE);
            response.setStatus(ReportStatus.FAKE);
        } else {
            report.setStatus(ReportStatus.HANDLED);
            response.setStatus(ReportStatus.HANDLED);
        }

        reportRepository.save(report);
        return responseRepository.save(response);
    }

    /**
     * دریافت تمام پاسخ‌های سیستم
     *
     * @return لیست پاسخ‌ها
     */
    public List<Response> getAllResponses() {
        return responseRepository.findAll();
    }

    /**
     * دریافت پاسخ‌های یک گزارش خاص
     *
     * @param reportId شناسه گزارش
     * @return لیست پاسخ‌های مرتبط
     */
    public List<Response> getResponsesByReportId(final Long reportId) {
        return responseRepository.findByReportId(reportId);
    }

    /**
     * ذخیره پاسخ با وضعیت مشاهده شده
     *
     * @param response اطلاعات پاسخ
     * @return پاسخ ذخیره شده
     */
    @Transactional
    public Response saveSeenResponse(final Response response) {
        response.setSeen(true);
        response.setSeenAt(LocalDateTime.now());
        response.setResponseTime(LocalDateTime.now());
        return responseRepository.save(response);
    }

    /**
     * حذف پاسخ
     *
     * @param responseId شناسه پاسخ
     */
    @Transactional
    public void deleteResponse(final Long responseId) {
        responseRepository.deleteById(responseId);
    }
}











//package com.example.demo.service;
//
//import com.example.demo.model.Report;
//import com.example.demo.model.Response;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.repository.ResponseRepository;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class ResponseService {
//
//    private final ResponseRepository responseRepository;
//    private final ReportRepository reportRepository;
//
//    public ResponseService(ResponseRepository responseRepository, ReportRepository reportRepository) {
//        this.responseRepository = responseRepository;
//        this.reportRepository = reportRepository;
//    }
//
//    /**
//     * ایجاد پاسخ جدید برای یک گزارش
//     * @param reportId شناسه گزارش مربوطه
//     * @param content محتوای پاسخ
//     * @param responder نام پاسخ دهنده
//     * @return پاسخ ایجاد شده
//     * @throws RuntimeException اگر گزارش مربوطه یافت نشود
//     * @PreAuthorize دسترسی فقط برای ADMIN و RESPONSE
//     */
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public Response createResponse(Long reportId, String content, String responder) {
//        Report report = reportRepository.findById(reportId)
//                .orElseThrow(() -> new RuntimeException("گزارش مورد نظر یافت نشد"));
//
//        Response response = new Response();
//        response.setReport(report);
//        response.setContent(content);
//        response.setResponder(responder);
//        response.setCreatedAt(LocalDateTime.now());
//
//        return responseRepository.save(response);
//    }
//
//    /**
//     * دریافت تمام پاسخ‌های سیستم
//     * @return لیست تمام پاسخ‌ها
//     * @PreAuthorize دسترسی فقط برای ADMIN و RESPONSE
//     */
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public List<Response> getAllResponses() {
//        return responseRepository.findAll();
//    }
//
//    /**
//     * دریافت پاسخ‌های مربوط به یک گزارش خاص
//     * @param reportId شناسه گزارش
//     * @return لیست پاسخ‌های مرتبط با گزارش
//     * @PreAuthorize دسترسی فقط برای ADMIN و RESPONSE
//     */
//    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSE')")
//    public List<Response> getResponsesByReportId(Long reportId) {
//        return responseRepository.findByReportId(reportId);
//    }
//
//    /**
//     * حذف یک پاسخ از سیستم
//     * @param responseId شناسه پاسخ مورد نظر
//     * @PreAuthorize دسترسی فقط برای ADMIN
//     */
//    @PreAuthorize("hasRole('ADMIN')")
//    public void deleteResponse(Long responseId) {
//        responseRepository.deleteById(responseId);
//    }
//}
//
















//
//package com.example.demo.service;
//
//import com.example.demo.model.Report;
//import com.example.demo.model.ReportStatus;
//import com.example.demo.model.Response;
//import com.example.demo.repository.ReportRepository;
//import com.example.demo.repository.ResponseRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class ResponseService {
//
//    private final ResponseRepository responseRepository;
//    private final ReportRepository reportRepository;
//@Autowired
//    public ResponseService(ResponseRepository responseRepository, ReportRepository reportRepository) {
//        this.responseRepository = responseRepository;
//        this.reportRepository = reportRepository;
//    }
//
//    public Response respondToReport(Response response) {
//        Report report = response.getReport();
//
//        if (!response.isSeen()) {
//            // وضعیت گزارش را به UNSEEN تغییر بده و ذخیره کن
//            report.setSatus(ReportStatus.UNSEEN);
//            reportRepository.save(report);
//            throw new IllegalStateException("امدادگر هنوز گزارش را مشاهده نکرده است. وضعیت گزارش به UNSEEN تغییر کرد.");
//        }
//
//        response.setResponseTime(LocalDateTime.now());
//
//        if (response.getSeenAt() == null) {
//            response.setSeenAt(LocalDateTime.now());
//        }
//
//        // تنظیم وضعیت گزارش بر اساس نتیجه پاسخ
//        if ("FAKE".equalsIgnoreCase(response.getResult())) {
//            report.setstatus(ReportStatus.FAKE);
//        } else {
//            report.setStatus(ReportStatus.HANDLED);
//        }
//
//        reportRepository.save(report);
//        return responseRepository.save(response);
//    }
//
//
//    public List<Response> getAllResponses() {
//        return responseRepository.findAll();
//    }
//    public Response saveSeenResponse(Response response) {
//        response.setResponseTime(LocalDateTime.now());
//        return responseRepository.save(response);
//    }
//
//}
//
