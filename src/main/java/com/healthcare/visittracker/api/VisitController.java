package com.healthcare.visittracker.api;

import com.healthcare.visittracker.dto.VisitRequest;
import com.healthcare.visittracker.dto.VisitDto;
import com.healthcare.visittracker.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @PostMapping
    public ResponseEntity<VisitDto> createVisit(@Valid @RequestBody VisitRequest request) {
        return ResponseEntity.ok(visitService.createVisit(request));
    }
}
