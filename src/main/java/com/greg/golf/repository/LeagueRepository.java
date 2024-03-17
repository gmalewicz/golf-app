package com.greg.golf.repository;

import com.greg.golf.entity.League;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    List<League> findAllByOrderByIdDesc(Pageable pageable);
}
