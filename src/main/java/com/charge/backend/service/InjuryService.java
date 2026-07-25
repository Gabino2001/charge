package com.charge.backend.service;

import com.charge.backend.domain.Injury;
import com.charge.backend.domain.User;
import com.charge.backend.dto.InjuryDtos.CreateInjuryRequest;
import com.charge.backend.dto.InjuryDtos.InjuryResponse;
import com.charge.backend.dto.InjuryDtos.UpdateInjuryRequest;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.InjuryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InjuryService {

    private final InjuryRepository injuryRepository;
    private final PlayerService playerService;

    @Transactional
    public InjuryResponse create(Long coachId, Long playerId, CreateInjuryRequest request) {
        User player = playerService.getPlayerOwnedByCoach(coachId, playerId);
        Injury injury = Injury.builder()
                .player(player)
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .build();
        injuryRepository.save(injury);
        return toResponse(injury);
    }

    public List<InjuryResponse> listForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return injuryRepository.findByPlayerIdOrderByStartDateDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InjuryResponse update(Long coachId, Long injuryId, UpdateInjuryRequest request) {
        Injury injury = injuryRepository.findById(injuryId)
                .orElseThrow(() -> new ResourceNotFoundException("Blessure introuvable : " + injuryId));
        if (injury.getPlayer().getCoach() == null || !injury.getPlayer().getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Cette blessure ne fait pas partie de votre effectif.");
        }
        injury.setStatus(request.status());
        injury.setEndDate(request.endDate());
        injuryRepository.save(injury);
        return toResponse(injury);
    }

    private InjuryResponse toResponse(Injury i) {
        return new InjuryResponse(i.getId(), i.getTitle(), i.getDescription(), i.getStatus(), i.getStartDate(), i.getEndDate());
    }
}
