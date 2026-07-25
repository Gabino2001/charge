package com.charge.backend.controller;

import com.charge.backend.dto.AccountDtos.ChangePasswordRequest;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** Accessible au préparateur comme au joueur : chacun change son propre mot de passe. */
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(CurrentUser.id(), request);
        return ResponseEntity.noContent().build();
    }
}
