package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.OnlineRound;

@Repository
public interface OnlineRoundRepository extends JpaRepository<OnlineRound, Long> {

	@EntityGraph(attributePaths = {"scoreCard", "course", "courseTee", "player"})
	List<OnlineRound> findByCourse(Course course);
	
	
	@EntityGraph(attributePaths = {"course", "courseTee", "player"})
	List<OnlineRound> findAll();


	void deleteByOwnerAndFinalized(Long ownerId, Boolean finzalized);
	
	@EntityGraph(attributePaths = {"scoreCard", "player", "course", "courseTee"})
	List<OnlineRound> findByOwner(Long owner);
	
	@EntityGraph(attributePaths = {"scoreCard", "player", "course", "courseTee"})
	List<OnlineRound> findByOwnerAndFinalized(Long owner, Boolean finalized);
	
}
