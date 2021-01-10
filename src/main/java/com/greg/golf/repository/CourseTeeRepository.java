package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;

@Repository
public interface CourseTeeRepository extends JpaRepository<CourseTee, Long> {

	List<CourseTee> findByCourse(Course course);
}
