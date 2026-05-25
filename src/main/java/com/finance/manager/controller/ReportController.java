package com.finance.manager.controller;

import com.finance.manager.dto.ReportDto;
import com.finance.manager.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ReportDto.MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        ReportDto.MonthlyReportResponse response = reportService.getMonthlyReport(year, month);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<ReportDto.YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        ReportDto.YearlyReportResponse response = reportService.getYearlyReport(year);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
