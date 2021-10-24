package com.greg.golf.service;

import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.repository.CycleRepository;
import com.greg.golf.repository.CycleTournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "cycle")
@Service("cycleService")
public class CycleService {

    private final CycleRepository cycleRepository;

    private final CycleTournamentRepository cycleTournamentRepository;

    @Transactional
    public Cycle addCycle(Cycle cycle) {

        return cycleRepository.save(cycle);
    }

    @Transactional
    public CycleTournament addCycleTournament(CycleTournament cycleTournament) {

        return cycleTournamentRepository.save(cycleTournament);
    }

    @Transactional(readOnly = true)
    public List<Cycle> findAllCycles() {
        return cycleRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public List<CycleTournament> findAllCycleTournaments(Long cycleId) {
        var cycle = new Cycle();
        cycle.setId(cycleId);
        return cycleTournamentRepository.findByCycleOrderByStartDate(cycle);
    }
}
