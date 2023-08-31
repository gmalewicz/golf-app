package com.greg.golf.service;

import com.greg.golf.entity.League;
import com.greg.golf.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service("leagueService")
public class LeagueService {

    private final LeagueRepository leagueRepository;

    @Transactional
    public League addLeague(League league) {

        return leagueRepository.save(league);
    }

    @Transactional(readOnly = true)
    public List<League> findAllLeagues() {
        return leagueRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

}
