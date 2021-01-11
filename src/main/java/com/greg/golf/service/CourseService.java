package com.greg.golf.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.CourseRepository;
import com.greg.golf.repository.CourseTeeRepository;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.HoleRepository;

@Service("courseService")
public class CourseService {

	@Autowired
	private CourseRepository courseRepository;
	
	@Autowired
	private CourseTeeRepository courseTeeRepository;
	
	@Autowired
	private FavouriteCourseRepository favouriteCourseRepository;

	@Autowired
	private HoleRepository holeRepository;

	
	@Transactional(readOnly=false)
	public long deleteFromFavourites(Course course, Long playerId) {
		
		Player player = new Player();
		player.setId(playerId);
				
		return favouriteCourseRepository.deleteByPlayerAndCourse(player, course);
	}
	
	@Transactional(readOnly=false)
	public void addToFavourites(Course course, Long playerId) {
		
		Player player = new Player();
		player.setId(playerId);
		FavouriteCourse favouriteCourse = new FavouriteCourse(); 
		favouriteCourse.setPlayer(player);
		favouriteCourse.setCourse(course);
		
		favouriteCourseRepository.save(favouriteCourse);
	}
	
	
	@Transactional(readOnly=true)
	public List<Course> list() {
		return courseRepository.findByOrderByNameAsc();
	}
	
	@Transactional(readOnly=true)
	public List<Course> listFavourites(Long playerId) {
		
		Player player = new Player();
		player.setId(playerId);
		
		return listFavourites(player);
	}
	
	
	@Transactional(readOnly=true)
	public List<Course> listFavourites(Player player) {
		List<FavouriteCourse> favouriteCourses = favouriteCourseRepository.findByPlayer(player);
		
		return favouriteCourses.stream().map(FavouriteCourse::getCourse).collect(Collectors.toList());
	}
	

	@Transactional(readOnly=true)
	public List<Hole> getHoles(Course course) {
		return holeRepository.findByCourse(course);
	}

	@Transactional(readOnly=true)
	public Optional<Course> getCourse(Long id) {
		return courseRepository.findById(id);
	}

	@Transactional
	public Course save(Course course) {

		course.getHoles().stream().forEach(h -> h.setCourse(course));
		course.getTees().stream().forEach(h -> h.setCourse(course));

		return courseRepository.save(course);
	}

	@Transactional
	public void delete(Long id) {

		courseRepository.deleteById(id);

	}
	
	@Transactional(readOnly=true)
	public List<CourseTee> getTees(Long id) {
		
		Course course = new Course();
		course.setId(id);

		return courseTeeRepository.findByCourse(course);

	}
	
	@Transactional(readOnly=true)
	public Optional<CourseTee> getTeeByid(Long id) {
		
		return courseTeeRepository.findById(id);

	}

}
