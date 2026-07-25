package com.charge.backend.controller;

import com.charge.backend.dto.InjuryDtos.CreateInjuryRequest;
import com.charge.backend.dto.InjuryDtos.InjuryResponse;
import com.charge.backend.dto.InjuryDtos.UpdateInjuryRequest;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.InjuryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class InjuryController {

    private final InjuryService injuryService;

    @PostMapping("/api/players/{playerId}/injuries")
    public ResponseEntity<InjuryResponse> create(@PathVariable Long playerId, @Valid @RequestBody CreateInjuryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(injuryService.create(CurrentUser.id(), playerId, request));
    }

    @GetMapping("/api/players/{playerId}/injuries")
    public List<InjuryResponse> list(@PathVariable Long playerId) {
        return injuryService.listForPlayer(CurrentUser.id(), playerId);
    }

    @PatchMapping("/api/injuries/{injuryId}")
    public InjuryResponse update(@PathVariable Long injuryId, @Valid @RequestBody UpdateInjuryRequest request) {
        return injuryService.update(CurrentUser.id(), injuryId, request);
    }
}
