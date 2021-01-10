package com.greg.golf.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Tournament;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

}
