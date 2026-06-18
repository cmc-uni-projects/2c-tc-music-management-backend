package com.example.CMCmp3.controller;

import com.example.CMCmp3.dto.StatsSummaryDTO;
import com.example.CMCmp3.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryDTO> getSummary() {
        return ResponseEntity.ok(statsService.getStatsSummary());
    }

}
