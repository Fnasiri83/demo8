package com.example.demo.repository;

import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * جستجوی گزارشات در محدوده جغرافیایی مشخص
     * @param minLat حداقل عرض جغرافیایی
     * @param maxLat حداکثر عرض جغرافیایی
     * @param minLon حداقل طول جغرافیایی
     * @param maxLon حداکثر طول جغرافیایی
     * @return لیست گزارشات در محدوده مشخص شده
     */
    List<Report> findByLatitudeBetweenAndLongitudeBetween(
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon);

    /**
     * شمارش گزارشات ایجاد شده پس از تاریخ مشخص
     * @param dateTime تاریخ و زمان مبدأ
     * @return تعداد گزارشات
     */
    long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);

    /**
     * آمار گزارشات بر اساس نوع حادثه
     * @return مپ شامل (نوع حادثه -> تعداد)
     */
    @Query("SELECT r.incidentType, COUNT(r) FROM Report r GROUP BY r.incidentType")
    Map<String, Long> countReportsByIncidentType();

    /**
     * شمارش گزارشات جعلی
     * @return تعداد گزارشات جعلی
     */
    long countByIsFakeTrue();

    /**
     * شمارش گزارشات واقعی
     * @return تعداد گزارشات واقعی
     */
    long countByIsFakeFalse();

    /**
     * شمارش گزارشات رسیدگی شده
     * @return تعداد گزارشات رسیدگی شده
     */
    long countByResolvedTrue();

    /**
     * شمارش گزارشات در انتظار رسیدگی
     * @return تعداد گزارشات در انتظار
     */
    long countByResolvedFalse();

    /**
     * شمارش گزارشات امروز
     * @return تعداد گزارشات امروز
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE DATE(r.createdAt) = CURRENT_DATE")
    long countReportsToday();

    /**
     * شمارش گزارشات هفته جاری
     * @param lastWeekDate تاریخ شروع هفته جاری
     * @return تعداد گزارشات هفته جاری
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt >= :lastWeekDate")
    long countReportsThisWeek(@Param("lastWeekDate") LocalDateTime lastWeekDate);

    /**
     * شمارش گزارشات ماه جاری
     * @return تعداد گزارشات ماه جاری
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE FUNCTION('MONTH', r.createdAt) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', r.createdAt) = FUNCTION('YEAR', CURRENT_DATE)")
    long countReportsThisMonth();

    /**
     * شمارش گزارشات سال جاری
     * @return تعداد گزارشات سال جاری
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE FUNCTION('YEAR', r.createdAt) = FUNCTION('YEAR', CURRENT_DATE)")
    long countReportsThisYear();

    /**
     * آمار گزارشات بر اساس وضعیت اعتبار
     * @return لیستی از آرایه‌های آبجکت شامل (وضعیت اعتبار، تعداد)
     */
    @Query("SELECT r.valid, COUNT(r) FROM Report r GROUP BY r.valid")
    List<Object[]> countReportsByValidity();

    /**
     * آمار گزارشات بر اساس وضعیت پردازش
     * @return مپ شامل (وضعیت پردازش -> تعداد)
     */
    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    Map<ReportStatus, Long> countReportsByStatus();

    /**
     * شمارش گزارشات ایجاد شده پس از تاریخ مشخص
     * @param date تاریخ مبدأ
     * @return تعداد گزارشات
     */
    long countByCreatedAtAfter(@Param("date") LocalDate date);

    Long countByStatus(ReportStatus status);
}

















//package com.example.demo.repository;
//
//import com.example.demo.model.Report;
//import com.example.demo.model.ReportStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//@Repository
//public interface ReportRepository extends JpaRepository<Report, Long> {
//    List<Report> findByLatitudeBetweenAndLongitudeBetween(
//            double minLat, double maxLat,
//            double minLon, double maxLon);
//    long countByCreatedAtAfter(LocalDateTime dateTime);
//
////    آمار براساس نوع حادثه
////   @Query("SELECT  COUNT(r) FROM Report r GROUP BY r.incidentType.title")
////    List<Object[]> countReportsByIncidentType();
//
//    // آمار واقعی یا جعلی
//    long countByIsFakeTrue();
//    long countByIsFakeFalse();
//
//    // آمار گزارشات رسیدگی‌شده
//    long countByResolvedTrue();
//    long countByResolvedFalse();
//
//    @Query("SELECT COUNT(r) FROM Report r WHERE DATE(r.createdAt) = CURRENT_DATE")
//    Long countReportsToday();
//    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt > :lastWeekDate")
//    Long countReportsLastWeek(@Param("lastWeekDate") LocalDate lastWeekDate);
//
//    @Query("SELECT COUNT(r) FROM Report r WHERE FUNCTION('MONTH', r.createdAt) = FUNCTION('MONTH', CURRENT_DATE) AND FUNCTION('YEAR', r.createdAt) = FUNCTION('YEAR', CURRENT_DATE)")
//   Long countReportsThisMonth();
//    @Query("SELECT r.isValid, COUNT(r) FROM Report r GROUP BY r.isValid")
//    List<Object[]> countReportsByValidity();
//    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt > :lastWeekDate")
//    Long countReportsAfter(@Param("lastWeekDate") LocalDate lastWeekDate);
//
//
//    Map<String, Long> countReportsByIncidentType(String incidentType);
//
//    List<Report> incidentType(String incidentType);
//
//    Long countByStatus(ReportStatus status);
//}