package com.charge.backend.controller;

import com.charge.backend.dto.PlayerDtos.CreatePlayerRequest;
import com.charge.backend.dto.PlayerDtos.PlayerResponse;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        PlayerResponse response = playerService.createPlayer(CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<PlayerResponse> listPlayers() {
        return playerService.listPlayers(CurrentUser.id());
    }

    @GetMapping("/{playerId}")
    public PlayerResponse getPlayer(@PathVariable Long playerId) {
        return playerService.getPlayer(CurrentUser.id(), playerId);
    }

    /** Supprime un joueur et toutes ses données associées. Action irréversible. */
    @DeleteMapping("/{playerId}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long playerId) {
        playerService.deletePlayer(CurrentUser.id(), playerId);
        return ResponseEntity.noContent().build();
    }
}
