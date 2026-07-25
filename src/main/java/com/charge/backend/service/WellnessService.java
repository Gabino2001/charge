package com.charge.backend.service;

import com.charge.backend.domain.User;
import com.charge.backend.domain.WellnessEntry;
import com.charge.backend.dto.WellnessDtos.SubmitWellnessRequest;
import com.charge.backend.dto.WellnessDtos.WellnessResponse;
import com.charge.backend.dto.WellnessDtos.WellnessStatus;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.UserRepository;
import com.charge.backend.repository.WellnessEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WellnessService {

    private final WellnessEntryRepository wellnessEntryRepository;
    private final UserRepository userRepository;

    @Transactional
    public WellnessResponse submitToday(Long playerId, SubmitWellnessRequest request) {
        LocalDate today = LocalDate.now();
        if (wellnessEntryRepository.existsByPlayerIdAndEntryDate(playerId, today)) {
            throw new IllegalArgumentException("Le questionnaire de bien-être a déjà été rempli aujourd'hui.");
        }
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur introuvable : " + playerId));

        boolean painReported = request.soreness() != null && request.soreness() <= 2;

        WellnessEntry entry = WellnessEntry.builder()
                .player(player)
                .entryDate(today)
                .mood(request.mood())
                .sleep(request.sleep())
                .fatigue(request.fatigue())
                .soreness(request.soreness())
                .stress(request.stress())
                .painLocation(painReported ? request.painLocation() : null)
                .build();
        wellnessEntryRepository.save(entry);
        return toResponse(entry);
    }

    public WellnessStatus getTodayStatus(Long playerId) {
        LocalDate today = LocalDate.now();
        return wellnessEntryRepository.findByPlayerIdAndEntryDate(playerId, today)
                .map(entry -> new WellnessStatus(true, toResponse(entry)))
                .orElse(new WellnessStatus(false, null));
    }

    public List<WellnessResponse> history(Long playerId) {
        return wellnessEntryRepository.findByPlayerIdOrderByEntryDateDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Utilisé par les autres services (exercices) pour appliquer la règle de blocage. */
    public boolean hasSubmittedToday(Long playerId) {
        return wellnessEntryRepository.existsByPlayerIdAndEntryDate(playerId, LocalDate.now());
    }

    private WellnessResponse toResponse(WellnessEntry e) {
        return new WellnessResponse(e.getId(), e.getEntryDate(), e.getMood(), e.getSleep(), e.getFatigue(), e.getSoreness(), e.getStress(), e.getPainLocation());
    }
}
