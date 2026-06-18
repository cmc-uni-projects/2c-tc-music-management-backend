package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.ReportDTO;
import com.example.CMCmp3.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports") // Admin prefix for clarity
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // All endpoints in this controller require ADMIN role
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/pending")
    public ResponseEntity<List<ReportDTO>> getPendingReports() {
        List<ReportDTO> pendingReports = reportService.getPendingReports();
        return ResponseEntity.ok(pendingReports);
    }

    @PostMapping("/{reportId}/approve")
    public ResponseEntity<Void> approveReport(@PathVariable Long reportId) {
        reportService.approveReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/reject")
    public ResponseEntity<Void> rejectReport(@PathVariable Long reportId) {
        reportService.rejectReport(reportId);
        return ResponseEntity.noContent().build();
    }
}
