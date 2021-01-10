package com.greg.golf.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
	
	 List<Round> findByPlayer(Player player);
	
	//@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	//@Modifying
	 @Query("SELECT r FROM Round r where r.course = :course AND r.roundDate = :roundDate") 
	 Optional<Round> findByCourseDateTeeTime(@Param("course") Course course, @Param("roundDate") Date roundDate);

	 List<Round> findByPlayerOrderByRoundDateDesc(Player player);
	 
	 List<Round> findByTournamentIsNullAndRoundDateBetween(Date startDate, Date endDate);

	 @EntityGraph(attributePaths = { "course"})
	 List<Round> findByPlayerOrderByRoundDateDesc(Player player, Pageable pageable);
	 
	 @EntityGraph(attributePaths = { "course"})
	 List<Round> findByOrderByRoundDateDesc(Pageable pageable);
}
