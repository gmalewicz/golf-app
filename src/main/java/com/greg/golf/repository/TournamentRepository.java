package com.greg.golf.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Tournament;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament>  findAllByOrderByIdDesc(Pageable pageable);

}
