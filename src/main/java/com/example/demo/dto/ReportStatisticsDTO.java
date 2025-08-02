package com.example.demo.dto;

import com.example.demo.model.Report;

import java.util.Map;

public class ReportStatisticsDTO {
    private long todayCount;
    private long weekCount;
    private long monthCount;
    private long yearCount;




    private Map<String, Long> countByIncidentType;
    private long validReports;
    private long fakeReports;
    private Map<String, Long> outcomeStats;// outcome مثل "Solved", "Pending", "Escalated"



    public long getTodayCount() {
        return todayCount;
    }
    public void setTodayCount(long todayCount) {
        this.todayCount = todayCount;
    }
    public long getWeekCount() {
        return weekCount;
    }
    public void setWeekCount(long weekCount) {
        this.weekCount = weekCount;
    }
    public long getMonthCount() {
        return monthCount;
    }
    public void setMonthCount(long monthCount) {
        this.monthCount = monthCount;
    }
    public long getYearCount() {
        return yearCount;
    }
    public void setYearCount(long yearCount) {
        this.yearCount = yearCount;
    }
    public Map<String, Long> getCountByIncidentType() {
        return countByIncidentType;
    }
    public void setCountByIncidentType(Map<String, Long> countByIncidentType) {
        this.countByIncidentType = countByIncidentType;
    }
    public long getValidReports() {
        return validReports;
    }
    public void setValidReports(long validReports) {
        this.validReports = validReports;
    }
    public long getFakeReports() {
        return fakeReports;
    }
    public void setFakeReports(long fakeReports) {
        this.fakeReports = fakeReports;
    }
    public Map<String, Long> getOutcomeStats() {
        return outcomeStats;
    }
    public void setOutcomeStats(Map<String, Long> outcomeStats) {
        this.outcomeStats = outcomeStats;
    }

}