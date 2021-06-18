package com.greg.golf.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.configurationproperties.CourseServiceConfig;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.repository.CourseRepository;
import com.greg.golf.repository.CourseTeeRepository;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.HoleRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ConfigurationProperties(prefix = "course")
@Service("courseService")
public class CourseService {
	
	private final CourseServiceConfig courseServiceConfig;
	private final CourseRepository courseRepository;
	private final CourseTeeRepository courseTeeRepository;
	private final FavouriteCourseRepository favouriteCourseRepository;
	private final HoleRepository holeRepository;
	
	@Transactional(readOnly=true)
	public List<Course> searchForCourses(String courseName) {
		
		if (courseName.length() < courseServiceConfig.getMinSearchLength()) {
			throw new TooShortStringForSearchException();
		}
		
		return courseRepository.findByHistoricalAndNameContainingIgnoreCase(courseName, false);
	}
	
	@Transactional(readOnly=false)
	public long deleteFromFavourites(Course course, Long playerId) {
		
		var player = new Player();
		player.setId(playerId);
				
		return favouriteCourseRepository.deleteByPlayerAndCourse(player, course);
	}
	
	@Transactional(readOnly=false)
	public void addToFavourites(Course course, Long playerId) {
		
		var player = new Player();
		player.setId(playerId);
		var favouriteCourse = new FavouriteCourse(); 
		favouriteCourse.setPlayer(player);
		favouriteCourse.setCourse(course);
		
		favouriteCourseRepository.save(favouriteCourse);
	}
	
	
	@Transactional(readOnly=true)
	public List<Course> list() {
		return courseRepository.findByHistoricalOrderByNameAsc(false);
	}
	
	@Transactional(readOnly=true)
	public List<Course> listFavourites(Long playerId) {
		
		var player = new Player();
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
		course.setHistorical(false);

		return courseRepository.save(course);
	}

	@Transactional
	public void delete(Long id) {

		courseRepository.deleteById(id);

	}
	
	@Transactional(readOnly=true)
	public List<CourseTee> getTees(Long id) {
		
		var course = new Course();
		course.setId(id);

		return courseTeeRepository.findByCourse(course);

	}
	
	@Transactional(readOnly=true)
	public Optional<CourseTee> getTeeByid(Long id) {
		
		return courseTeeRepository.findById(id);

	}
	
	@Transactional(readOnly = true)
	public List<Course> getSortedCourses(Integer pageNo) {

		return courseRepository.findByHistoricalOrderByNameAsc(false, PageRequest.of(pageNo, courseServiceConfig.getPageSize()));
	}

}
