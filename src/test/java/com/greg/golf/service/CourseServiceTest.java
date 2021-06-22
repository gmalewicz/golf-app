package com.greg.golf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CourseServiceTest {
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	@Autowired
	private CourseService courseService;
	
	@BeforeAll
	public static void setup(@Autowired PlayerRepository playerRepository) {
/*
		FavouriteCourse fc = new FavouriteCourse();
		Player player = new Player();
		player.setId(1L);
		Course course = new Course();
		course.setId(1L);
		fc.setPlayer(player);
		fc.setCourse(course);
		
		favouriteCourseRepository.save(fc);
		*/
		log.info("Set up completed");
	}

	@DisplayName("Get list of courses")
	@Transactional
	@Test
	void getListOfCoursesTest() {

		List<Course> courses = courseService.list();
		assertEquals(1, courses.size());
	}
	
	@DisplayName("Get list of holes")
	@Transactional
	@Test
	void getListOfHolesTest() {

		Course course = new Course();
		course.setId(1L);
		
		List<Hole> holes = courseService.getHoles(course);
		assertEquals(18, holes.size());
	}
	
	@DisplayName("Save the correct course")
	@Transactional
	@Test
	void saveTheCorrectCourseTest() {

		Course course = new Course();
		course.setId(1L);
		course.setName("test");
		course.setPar(72);
		course.setHoleNbr(18);
		
		Hole hole = new Hole();
		hole.setNumber(1);
		hole.setPar(3);
		hole.setSi(1);
		List<Hole> holes = new ArrayList<Hole>();
		holes.add(hole);
		course.setHoles(holes);
		
		CourseTee courseTee = new CourseTee();
		courseTee.setSex(false);
		courseTee.setCr(30F);
		courseTee.setSr(55);
		courseTee.setTee("test");
		courseTee.setTeeType(Common.TEE_TYPE_18);
		List<CourseTee> tees = new ArrayList<CourseTee>();
		tees.add(courseTee);
		course.setTees(tees);
		
				
		course = courseService.save(course);
		assertNotNull(course.getId());
	}
	
	@DisplayName("Delete the course")
	@Transactional
	@Test
	void deleteCourseTest() {

		courseService.delete(1L);
		Optional<Course> course = courseService.getCourse(1L);
		
		assertEquals(false, course.isPresent());
	}
	
	@DisplayName("Get tees")
	@Transactional
	@Test
	void getTeesTest() {

		List<CourseTee> tees = courseService.getTees(1L);
		assertTrue(tees.size() > 0);
	}
	
	@DisplayName("Get tees no result")
	@Transactional
	@Test
	void getTeesNoResultTest() {

		List<CourseTee> tees = courseService.getTees(2L);
		assertEquals(0, tees.size());
	}
	
	
	@DisplayName("Get tee by id")
	@Transactional
	@Test
	void getTeesByIdTest() {

		Optional<CourseTee> tee = courseService.getTeeByid(1L);
		assertEquals(true, tee.isPresent());
	}
	
	@DisplayName("Get favourite courses for player")
	@Transactional
	@Test
	void getFavouruteCoursesTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {
		
		FavouriteCourse fc = new FavouriteCourse();
		Player player = new Player();
		player.setId(1L);
		Course course = new Course();
		course.setId(1L);
		fc.setPlayer(player);
		fc.setCourse(course);
		
		favouriteCourseRepository.save(fc);

		
		List<Course> fcRet = courseService.listFavourites(1L);

		assertEquals(1, fcRet.size());
	}
	
	@DisplayName("Add favourite course for player")
	@Transactional
	@Test
	void addFavouruteCourseTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {

		Course course = new Course();
		course.setId(1L);
		courseService.addToFavourites(course, 1L);
		
		List<Course> fc = courseService.listFavourites(1L);

		assertEquals(1, fc.size());
	}
	
	
	@DisplayName("Delete favourite course")
	@Transactional
	@Test
	void deleteFavouriteCourseTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {

		Course course = new Course();
		course.setId(1L);
		courseService.addToFavourites(course, 1L);
		long retVal = courseService.deleteFromFavourites(course, 1L);
		
	// 	List<Course> fc = courseService.listFavourites(1L);

		assertEquals(1, retVal);
	}
	
	@DisplayName("Serach for courses")
	@Transactional
	@Test
	void searchForCoursesTest() {
		
		List<Course> retVal =  courseService.searchForCourses("Sobie");
		
		assertEquals(1, retVal.size());
	}
	
	@DisplayName("Serach for too short course string")
	@Transactional
	@Test
	void searchForToShortCoursesTest() {
		
		assertThrows(TooShortStringForSearchException.class, () -> courseService.searchForCourses("So"));
		
	}
	
	@DisplayName("Get courses alphabetically")
	@Transactional
	@Test
	void searchForCoursesAlphabeticallyTest() {
		
		List<Course> retVal =  courseService.getSortedCourses(0);
		
		assertEquals(1, retVal.size());
	}
	
	@DisplayName("Should move course to history")
	@Transactional
	@Test
	void moveCourseToHistoryTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {
		
		// first add the course to favorites
		Course course = new Course();
		course.setId(1L);
		courseService.addToFavourites(course, 1L);
		
		//get the course
		course = courseService.getCourse(1L).get();
		
		//move to history
		courseService.moveToHistoryCurse(1L);
		
		//check if course has historical flag set
		assertTrue("Historical flag shell be set", course.getHistorical());
		
		//check if favorites are empty
		assertEquals(0, favouriteCourseRepository.findAll().size());	
	}
		
	@AfterAll
	public static void done() {

		// favouriteCourseRepository.deleteAll();
		
		log.info("Clean up completed");

	}

}
