package com.greg.golf.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
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
	
	 Optional<Round> findRoundByCourseAndRoundDate(Course course, Date roundDate);
	 
	 @EntityGraph(attributePaths = { "player"})
	 List<Round> findByRoundDateBetween(Date startDate, Date endDate);

	 @EntityGraph(attributePaths = { "course"})
	 List<Round> findByPlayerOrderByRoundDateDesc(Player player, Pageable pageable);

	 @Query("SELECT r.id FROM Round r ORDER BY r.id DESC") 
	 List<Long> getIdsForPage(Pageable pageable);
	 
	 @EntityGraph(attributePaths = { "course", "player"})
	 @Query("SELECT r FROM Round r WHERE r.id in (:ids) ORDER BY r.id DESC") 
	 List<Round> getForIds(@Param("ids") List<Long> ids);
	 
	 @EntityGraph(attributePaths = { "player"})
     @NonNull
     Optional<Round> findById(@NonNull Long id);
}
