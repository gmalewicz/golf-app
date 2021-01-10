package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Player;

@Repository
public interface FavouriteCourseRepository extends JpaRepository<FavouriteCourse, Long> {

	@EntityGraph(attributePaths = { "course" })
	List<FavouriteCourse> findByPlayer(Player player);

	long deleteByPlayerAndCourse(Player player, Course course);
}
