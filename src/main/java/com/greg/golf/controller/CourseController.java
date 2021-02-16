package com.greg.golf.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseNameDto;
import com.greg.golf.controller.dto.CourseTeeDto;
import com.greg.golf.controller.dto.HoleDto;

import com.greg.golf.entity.Course;

import com.greg.golf.service.CourseService;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Course API") })
public class CourseController {

	@Autowired
	private CourseService courseService;

	@Autowired
	private ModelMapper modelMapper;

	@Tag(name = "Course API")
	@Operation(summary = "Get list of courses.")
	@GetMapping(value = "/rest/Courses")
	public List<CourseDto> getCourses() {

		log.info("Requested list of courses");
		return mapList(courseService.list(), CourseDto.class);
	}

	@Tag(name = "Course API")
	@Operation(summary = "Get list of holes for course.")
	@GetMapping(value = "/rest/Holes/{id}")
	public List<HoleDto> getHoles(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable("id") Long id) {

		log.info("Requested list of holes for Course id -  " + id);

		Course searchCourse = new Course();
		searchCourse.setId(id);

		return mapList(courseService.getHoles(searchCourse), HoleDto.class);
	}

	@Tag(name = "Course API")
	@Operation(summary = "Get list of tees for course with given id.")
	@GetMapping(value = "/rest/Tee/{id}")
	public List<CourseTeeDto> getTees(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable("id") Long id) {

		log.info("Requested list of tees for Course id -  " + id);

		return mapList(courseService.getTees(id), CourseTeeDto.class);
	}

	@Tag(name = "Course API")
	@Operation(summary = "Add the new course.")
	@PostMapping(value = "/rest/Course")
	public HttpStatus addCourse(
			@Parameter(description = "Course object", required = true) @Valid @RequestBody CourseDto courseDto) {

		log.info("trying to add course: " + courseDto);

		// copy data from dto to the entity object
		Course course = modelMapper.map(courseDto, Course.class);

		courseService.save(course);

		return HttpStatus.OK;
	}

	@Tag(name = "Course API")
	@Operation(summary = "Delete course with given id.")
	@DeleteMapping("/rest/Course/{id}")
	public ResponseEntity<Long> deleteCourse(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to delete course: " + id);

		try {
			courseService.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(id, HttpStatus.OK);

	}

	@Tag(name = "Round API")

	@Tag(name = "Course API")
	@Operation(summary = "Get list of favourite courses")
	@GetMapping(value = "/rest/FavouriteCourses/{playerId}")
	public List<CourseDto> getFavouriteCourses(
			@Parameter(description = "player id", example = "1", required = true) @PathVariable("playerId") Long playerId) {

		log.info("Requested list of favourite courses");

		return mapList(courseService.listFavourites(playerId), CourseDto.class);
	}

	@Tag(name = "Course API")
	@Operation(summary = "Add course to favourites")
	@PostMapping(value = "/rest/FavouriteCourses/{playerId}")
	public HttpStatus addCourseToFavourites(
			@Parameter(description = "player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "course object", required = true) @RequestBody CourseDto courseDto) {

		log.info("trying add course to favourites for player: " + playerId);

		courseService.addToFavourites(modelMapper.map(courseDto, Course.class), playerId);

		return HttpStatus.OK;
	}

	@Tag(name = "Course API")
	@Operation(summary = "Delete course from favourites")
	@PostMapping(value = "/rest/DeleteFavouriteCourse/{playerId}")
	public HttpStatus deleteCourseFromFavourites(
			@Parameter(description = "player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "course object", required = true) @RequestBody CourseDto courseDto) {

		log.info("trying to delete course from favourites for player: " + playerId);

		courseService.deleteFromFavourites(modelMapper.map(courseDto, Course.class), playerId);

		return HttpStatus.OK;
	}

	@Tag(name = "Course API")
	@Operation(summary = "Get list of courses for given string.")
	@PostMapping(value = "/rest/SearchForCourse")
	public List<CourseDto> searchForCourses(
			@Valid @Parameter(description = "Course name object", required = true) @RequestBody CourseNameDto courseNameDto) {

		log.info("Requested search for courses for name: " + courseNameDto.getName());

		return mapList(courseService.searchForCourses(courseNameDto.getName()), CourseDto.class);
	}

	private <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
		return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
	}

}
