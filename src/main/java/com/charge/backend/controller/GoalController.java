package com.charge.backend.controller;

import com.charge.backend.dto.GoalDtos.CreateGoalRequest;
import com.charge.backend.dto.GoalDtos.GoalResponse;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/api/players/{playerId}/goals")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<GoalResponse> create(@PathVariable Long playerId, @Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(CurrentUser.id(), playerId, request));
    }

    @GetMapping("/api/players/{playerId}/goals")
    @PreAuthorize("hasRole('COACH')")
    public List<GoalResponse> listForPlayer(@PathVariable Long playerId) {
        return goalService.listForPlayer(CurrentUser.id(), playerId);
    }

    @DeleteMapping("/api/goals/{goalId}")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<Void> delete(@PathVariable Long goalId) {
        goalService.delete(CurrentUser.id(), goalId);
        return ResponseEntity.noContent().build();
    }

    /** Le joueur consulte ses propres objectifs. */
    @GetMapping("/api/goals/mine")
    @PreAuthorize("hasRole('PLAYER')")
    public List<GoalResponse> listMine() {
        return goalService.listMine(CurrentUser.id());
    }
}
