package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.CreateReportDTO;
import com.example.CMCmp3.dto.ReportDTO;
import com.example.CMCmp3.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports") // Separate from AdminReportController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only authenticated users can create reports
    public ResponseEntity<ReportDTO> createReport(@Valid @RequestBody CreateReportDTO dto) {
        ReportDTO newReport = reportService.createReport(dto);
        return new ResponseEntity<>(newReport, HttpStatus.CREATED);
    }
}
