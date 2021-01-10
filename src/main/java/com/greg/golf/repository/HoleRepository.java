package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.Hole;

@Repository
public interface HoleRepository extends JpaRepository<Hole, Integer> {
	
	
	@Query("SELECT h FROM Hole h where h.course = :course ORDER BY h.id") 
	List<Hole> findByCourse(@Param("course") Course course);

}
