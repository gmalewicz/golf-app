package com.greg.golf.controller;

import java.util.ArrayList;
import java.util.List;

import com.greg.golf.security.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseTeeDto;
import com.greg.golf.controller.dto.HoleDto;
import com.greg.golf.entity.Course;
import com.greg.golf.entity.FavouriteCourse;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.FavouriteCourseRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CourseControllerTest {

	@SuppressWarnings("unused")
	@MockitoBean
	private JwtRequestFilter jwtRequestFilter;
	
	@ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();
		
	
	private final CourseController courseController;
		
	@Autowired
	public CourseControllerTest(CourseController courseController) {

		this.courseController = courseController;
	}

	@DisplayName("Gets list of holes for course")
	@Transactional
	@Test
	void getListOfHolesTest() {

		List<HoleDto> holeLst = this.courseController.getHoles(1L);

		Assertions.assertEquals(18, holeLst.size());

	}

	@DisplayName("Add course test")
	@Transactional
	@Test
	void addCourseTest() {

		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);

		List<HoleDto> holeDtoLst = new ArrayList<>();

		for (int i = 0; i < 9; i++) {
			HoleDto holeDto = new HoleDto();
			holeDto.setNumber(i + 1);
			holeDto.setPar(4);
			holeDto.setSi(18);
			holeDtoLst.add(holeDto);
		}

		courseDto.setHoles(holeDtoLst);

		List<CourseTeeDto> courseTeeDtoLst = new ArrayList<>();
		CourseTeeDto courseTeeDto = new CourseTeeDto();
		courseTeeDto.setCr(71f);
		courseTeeDto.setSr(78);
		courseTeeDto.setSex(false);
		courseTeeDto.setTeeType(1);
		courseTeeDto.setTee("Ladies red 1-18");
		courseTeeDtoLst.add(courseTeeDto);

		courseDto.setTees(courseTeeDtoLst);

		HttpStatus status = this.courseController.addCourse(courseDto);

		Assertions.assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Get tees test")
	@Transactional
	@Test
	void getTeesTest() {

		List<CourseTeeDto> retTees = this.courseController.getTees(1L);

		Assertions.assertEquals(9, retTees.size());
	}
	
	@DisplayName("Get favourite course test")
	@Transactional
	@Test
	void getFavouriteCourseTest(@Autowired FavouriteCourseRepository favouriteCourseRepository) {

		FavouriteCourse fc = new FavouriteCourse();
		Player player = new Player();
		player.setId(1L);
		Course course = new Course();
		course.setId(1L);
		fc.setPlayer(player);
		fc.setCourse(course);
		
		favouriteCourseRepository.save(fc);
		
		List<CourseDto> retCourses = this.courseController.getFavouriteCourses(1L);
		
		Assertions.assertEquals(1, retCourses.size());
	}
	
	@DisplayName("Get course to favourites")
	@Transactional
	@Test
	void getCourseToFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1L);
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		HttpStatus status = this.courseController.addCourseToFavourites(1L, courseDto);
		
		Assertions.assertEquals(HttpStatus.OK, status);
	}
	
	@DisplayName("Delete course from favourites")
	@Transactional
	@Test
	void deleteCourseFromFavouritesTest() {
		
		CourseDto courseDto = new CourseDto();
		courseDto.setId(1L);
		courseDto.setName("Test course");
		courseDto.setHoleNbr(9);
		courseDto.setPar(36);
		this.courseController.addCourseToFavourites(1L, courseDto);
		HttpStatus status = this.courseController.deleteCourseFromFavourites(1L, courseDto);
		
		Assertions.assertEquals(HttpStatus.OK, status);
	}
}
