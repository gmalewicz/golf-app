package com.greg.golf.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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

@SuppressWarnings("unused")
@Slf4j
@RestController
@OpenAPIDefinition(tags = @Tag(name = "Course API"))
public class CourseController extends BaseController {

	private final CourseService courseService;

	public CourseController(ModelMapper modelMapper, CourseService courseService) {
		super(modelMapper);
		this.courseService = courseService;
	}

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

		var searchCourse = new Course();
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

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Course API")
	@Operation(summary = "Add the new course.")
	@PostMapping(value = "/rest/Course")
	public HttpStatus addCourse(
			@Parameter(description = "Course object", required = true) @Valid @RequestBody CourseDto courseDto) {

		log.info("trying to add course: " + courseDto);

		// copy data from dto to the entity object
		var course = modelMapper.map(courseDto, Course.class);

		courseService.save(course);

		return HttpStatus.OK;
	}

	@SuppressWarnings("SameReturnValue")
    @Tag(name = "Course API")
	@Operation(summary = "Delete course with given id.")
	@DeleteMapping("/rest/Course/{id}")
	public HttpStatus deleteCourse(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to delete course: " + id);

		courseService.delete(id);

		return HttpStatus.OK;

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

	@SuppressWarnings("SameReturnValue")
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

	@SuppressWarnings("SameReturnValue")
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

	@Tag(name = "Course API")
	@Operation(summary = "Get courses alphabetically")
	@GetMapping(value = "/rest/SortedCourses/{pageId}")
	public List<CourseDto> getSortedCourses(
			@Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") Integer pageId) {

		log.info("Requested list of sorted courses for page id " + pageId);

		return mapList(courseService.getSortedCourses(pageId), CourseDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Course API")
	@Operation(summary = "Purge historical courses from favourites")
	@PostMapping(value = "/rest/MoveToHistoryCourse/{courseId}")
	public HttpStatus moveToHistoryCurse(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable("courseId") Long courseId) {

		log.info("trying to move course to history");

		courseService.moveToHistoryCurse(courseId);

		return HttpStatus.OK;
	}

}
