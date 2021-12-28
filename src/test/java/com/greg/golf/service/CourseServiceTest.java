package com.greg.golf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.greg.golf.entity.Course;
import com.greg.golf.entity.CourseTee;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Hole;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CourseServiceTest {

	@SuppressWarnings("unused")
	@MockBean
	private JwtRequestFilter jwtRequestFilter;
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

	@Autowired
	private CourseService courseService;
	
	@BeforeAll
	public static void setup(@Autowired PlayerRepository playerRepository) {
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
		var holes = new ArrayList<Hole>();
		holes.add(hole);
		course.setHoles(holes);
		
		CourseTee courseTee = new CourseTee();
		courseTee.setSex(false);
		courseTee.setCr(30F);
		courseTee.setSr(55);
		courseTee.setTee("test");
		courseTee.setTeeType(Common.TEE_TYPE_18);
		var tees = new ArrayList<CourseTee>();
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
		
		assertFalse(course.isPresent());
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

		Optional<CourseTee> tee = courseService.getTeeById(1L);
		assertTrue(tee.isPresent());
	}
	
	@DisplayName("Get favourite courses for player")
	@Transactional
	@Test
	void getFavouriteCoursesTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {
		
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
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.ADMIN));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("authorized", "fake", authorities));
		
		// first add the course to favorites
		Course course = new Course();
		course.setId(1L);
		courseService.addToFavourites(course, 1L);
		
		//get the course
		course = courseService.getCourse(1L).orElse(null);
		
		//move to history
		courseService.moveToHistoryCurse(1L);
		
		//check if course has historical flag set
		assertNotNull(course, "Course must be set");
		assertTrue(course.getHistorical(), "Historical flag shell be set");
		
		//check if favorites are empty
		assertEquals(0, favouriteCourseRepository.findAll().size());	
	}
	
	@DisplayName("Should not move course to history by unathorized user")
	@Transactional
	@Test
	void moveCourseToHistoryByUnauthorizedUserTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {
		
		var authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(Common.PLAYER));
		
		SecurityContextHolder.getContext().setAuthentication(
			        new UsernamePasswordAuthenticationToken("unauthorized", "fake", authorities));
		
		// first add the course to favorites
		Course course = new Course();
		course.setId(1L);
		courseService.addToFavourites(course, 1L);

		// try to execute move operation - it shall throw exception
		assertThrows(UnauthorizedException.class, () -> courseService.moveToHistoryCurse(1L));

	}
		
	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
