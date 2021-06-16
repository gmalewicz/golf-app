package com.greg.golf.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
	
	 List<Round> findByPlayer(Player player);
	
	 Optional<Round> findRoundByCourseAndRoundDate(Course course, Date roundDate);
	 
	 @EntityGraph(attributePaths = { "player"})
	 List<Round> findByTournamentIsNullAndRoundDateBetween(Date startDate, Date endDate);

	 @EntityGraph(attributePaths = { "course"})
	 List<Round> findByPlayerOrderByRoundDateDesc(Player player, Pageable pageable);
	 
	 @EntityGraph(attributePaths = { "course", "player"})
	 List<Round> findByOrderByRoundDateDescPlayerAsc(Pageable pageable);
	 
	 @EntityGraph(attributePaths = { "player"})
	 Optional<Round> findById(Long id);
}
