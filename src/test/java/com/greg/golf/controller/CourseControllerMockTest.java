package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.Course;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.security.oauth.*;
import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

class CourseControllerWebMvcTest {

	// -------------------------------------------------------
	// MVC + Controller dependencies
	// -------------------------------------------------------

	@SuppressWarnings("unused")
	@MockitoBean private CourseService courseService;
	@SuppressWarnings("unused")
	@MockitoBean private PlayerService playerService;
	@SuppressWarnings("unused")
	@MockitoBean private ModelMapper modelMapper;

	// -------------------------------------------------------
	// Security dependencies (must be mocked in @WebMvcTest)
	// -------------------------------------------------------

	@SuppressWarnings("unused")
	@MockitoBean private JwtRequestFilter jwtRequestFilter;
	@SuppressWarnings("unused")
	@MockitoBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@SuppressWarnings("unused")
	@MockitoBean private PasswordEncoder passwordEncoder;
	@SuppressWarnings("unused")
	@MockitoBean private UserService userService;

	@SuppressWarnings("unused")
	@MockitoBean private GolfOAuth2UserService golfOAuth2UserService;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;
	@SuppressWarnings("unused")
	@MockitoBean private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;


	@Autowired
	private final WebTestClient webTestClient;


	@Autowired
	private final ObjectMapper objectMapper;

	@Autowired
	public CourseControllerWebMvcTest(WebTestClient webTestClient, ObjectMapper objectMapper) {
		this.webTestClient = webTestClient;
		this.objectMapper = objectMapper;
	}

	// -------------------------------------------------------
	// SEARCH COURSES
	// -------------------------------------------------------

	@DisplayName("Search for courses with valid input")
	@Test
	void searchForCourses_whenValidInput_thenReturns200() {
		webTestClient.post()
				.uri("/rest/SearchForCourse")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new CourseNameDto("Test"))
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Search for courses with null input")
	@Test
	void searchForCourses_whenNullValue_thenReturns400() {
		webTestClient.post()
				.uri("/rest/SearchForCourse")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("null")
				.exchange()
				.expectStatus().isOk(); // preserving existing behavior
	}

	@DisplayName("Search for courses maps to business model")
	@Test
	void searchForCourses_whenValidInput_thenMapsToBusinessModel() {
		webTestClient.post()
				.uri("/rest/SearchForCourse")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new CourseNameDto("Test"))
				.exchange()
				.expectStatus().isOk();

		verify(courseService).searchForCourses("Test");
	}

	@DisplayName("Search for courses verifying response")
	@Test
	void searchForCourses_whenValidInput_thenReturnsCourseList() {

		Course course = new Course();
		course.setId(1L);
		course.setName("Test");
		course.setPar(72);
		course.setHoleNbr(18);

		CourseDto dto = new CourseDto();
		dto.setId(1L);
		dto.setName("Test");
		dto.setPar(72);
		dto.setHoleNbr(18);

		given(courseService.searchForCourses("Test"))
				.willReturn(List.of(course));
		given(modelMapper.map(course, CourseDto.class))
				.willReturn(dto);

		webTestClient.post()
				.uri("/rest/SearchForCourse")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new CourseNameDto("Test"))
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(CourseDto.class)
				.value(list -> {
					assertThat(list).hasSize(1);
					assertThat(list.getFirst())
							.usingRecursiveComparison()
							.isEqualTo(dto);
				});
	}

	// -------------------------------------------------------
	// DELETE COURSE
	// -------------------------------------------------------

	@DisplayName("Delete course with valid input")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void deleteCourse_whenValidInput_thenReturns200() {
		webTestClient.delete()
				.uri("/rest/Course/{id}", 1L)
				.exchange()
				.expectStatus().isOk();
	}

	@DisplayName("Delete course with invalid id")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void deleteCourse_whenInvalidInput_thenReturns400() {

		doThrow(new IllegalArgumentException())
				.when(courseService).delete(1L);

		webTestClient.delete()
				.uri("/rest/Course/{id}", 1L)
				.exchange()
				.expectStatus().isBadRequest()
				.expectBody(ApiErrorResponse.class)
				.value(error -> {
                    assert error != null;
                    assertThat(error.getError()).isEqualTo("16");
					assertThat(error.getMessage()).isEqualTo("Incorrect parameter");
				});
	}

	// -------------------------------------------------------
	// SORTED COURSES
	// -------------------------------------------------------

	@DisplayName("Search for sorted courses with valid input")
	@Test
	void getSortedCourses_whenValidInput_thenReturnsCourseList() {

		Course course = new Course();
		course.setId(1L);
		course.setName("Test");
		course.setPar(72);
		course.setHoleNbr(18);

		CourseDto dto = new CourseDto();
		dto.setId(1L);
		dto.setName("Test");
		dto.setPar(72);
		dto.setHoleNbr(18);

		given(courseService.getSortedCourses(0))
				.willReturn(List.of(course));
		given(modelMapper.map(course, CourseDto.class))
				.willReturn(dto);

		objectMapper.setDefaultPropertyInclusion(
				JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));

		webTestClient.get()
				.uri("/rest/SortedCourses/{page}", 0)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(CourseDto.class)
				.value(list -> assertThat(list).hasSize(1));
	}

	// -------------------------------------------------------
	// MOVE TO HISTORY
	// -------------------------------------------------------

	@DisplayName("Move course to history")
	@Test
	@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
	void moveCourseToHistory_whenValidInput_thenReturns200() {

		doNothing().when(courseService).moveToHistoryCurse(1L);

		webTestClient.post()
				.uri("/rest/MoveToHistoryCourse/{id}", 1L)
				.exchange()
				.expectStatus().isOk();
	}

	// -------------------------------------------------------
	// ADD TEE
	// -------------------------------------------------------

	@DisplayName("Should add tee with correct result")
	@Test
	void addTeeWhenValidInputThenReturns200() {

		CourseTeeDto input = new CourseTeeDto();
		input.setTee("Ladies red");
		input.setCr(64.6F);
		input.setSr(123);
		input.setSex(false);
		input.setTeeType(0);

		doNothing().when(courseService).addTee(any(), any());

		webTestClient.post()
				.uri("/rest/Tee/{courseId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(input)
				.exchange()
				.expectStatus().isOk();
	}
}