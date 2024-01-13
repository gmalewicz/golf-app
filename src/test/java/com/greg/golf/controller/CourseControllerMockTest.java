package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import com.greg.golf.controller.dto.*;
import com.greg.golf.security.oauth.GolfAuthenticationFailureHandler;
import com.greg.golf.security.oauth.GolfAuthenticationSuccessHandler;
import com.greg.golf.security.oauth.GolfOAuth2UserService;
import com.greg.golf.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.entity.Course;
import com.greg.golf.error.ApiErrorResponse;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;

import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;

import static org.mockito.BDDMockito.*;

@Slf4j
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CourseController.class)
class CourseControllerMockTest {

	@MockBean
	private CourseService courseService;

	@MockBean
	private PlayerService playerService;

	@MockBean
	private JwtRequestFilter jwtRequestFilter;

	@MockBean
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@MockBean
	private ModelMapper modelMapper;

	@SuppressWarnings("unused")
	@MockBean
	private PasswordEncoder bCryptPasswordEncoder;

	@SuppressWarnings("unused")
	@MockBean
	private UserService userService;

	@SuppressWarnings("unused")
	@MockBean
	private GolfOAuth2UserService golfOAuth2UserService;

	@SuppressWarnings("unused")
	@MockBean
	private GolfAuthenticationSuccessHandler golfAuthenticationSuccessHandler;

	@SuppressWarnings("unused")
	@MockBean
	private GolfAuthenticationFailureHandler golfAuthenticationFailureHandler;

	private final MockMvc mockMvc;
	private final ObjectMapper objectMapper;
	
	@Autowired
	public CourseControllerMockTest(MockMvc mockMvc, ObjectMapper objectMapper) {
		this.mockMvc = mockMvc;
		this.objectMapper = objectMapper;
	}

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}
	
	@DisplayName("Search for courses with valid input")
	@Test
	void searchForCourses_whenValidInput_thenReturns200() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(courseNameDto))).andExpect(status().isOk());
	}

	@DisplayName("Search for courses with null input")
	@Test
	void searchForCourses_whenNullValue_thenReturns400() throws Exception {

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(null))).andExpect(status().isBadRequest());

	}

	@DisplayName("Search for courses map to business model")
	@Test
	void searchForCourses_whenValidInput_thenMapsToBusinessModel() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(courseNameDto))).andExpect(status().isOk());

		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(courseService, times(1)).searchForCourses(stringCaptor.capture());
		assertThat(stringCaptor.getValue()).isEqualTo("Test");
	}

	@DisplayName("Search for courses veryfying response")
	@Test
	void searchForCourses_whenValidInput_thenReturnsCourseList() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");
		Course c = new Course();
		c.setName("Test");
		c.setPar(72);
		c.setHoleNbr(18);
		c.setId(1L);

		List<Course> retVal = new ArrayList<>();
		retVal.add(c);

		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test");
		courseDto.setPar(72);
		courseDto.setHoleNbr(18);
		courseDto.setId(1L);

		when(courseService.searchForCourses("Test")).thenReturn(retVal);
		when(modelMapper.map(c, CourseDto.class)).thenReturn(courseDto);

		MvcResult mvcResult = mockMvc
				.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
						.content(objectMapper.writeValueAsString(courseNameDto)))
				.andExpect(status().isOk()).andReturn();

		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		objectMapper.setSerializationInclusion(Include.NON_EMPTY);

		assertThat(actualResponseBody)
				.isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(retVal));
	}

	@DisplayName("Delete course with valid input")
	@Test
	void deleteCourse_whenValidInput_thenReturns200() throws Exception {

		mockMvc.perform(delete("/rest/Course/1")).andExpect(status().isOk());
	}

	@DisplayName("Delete course with invalid id")
	@Test
	void deleteCourse_whenValidInput_thenReturns400_2() throws Exception {

		doThrow(new IllegalArgumentException()).when(courseService).delete(1L);
		MvcResult mvcResult = mockMvc.perform(delete("/rest/Course/1")).andExpect(status().isBadRequest()).andReturn();

		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		assertThat(actualResponseBody)
				.isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(new ApiErrorResponse("16", "Incorrect parameter")));
	}
	
	@DisplayName("Search for sorted courses with valid input")
	@Test
	void getSortedCourses_whenValidInput_thenReturnsCourseList() throws Exception {

		Course c = new Course();
		c.setName("Test");
		c.setPar(72);
		c.setHoleNbr(18);
		c.setId(1L);

		List<Course> retVal = new ArrayList<>();
		retVal.add(c);
		
		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test");
		courseDto.setPar(72);
		courseDto.setHoleNbr(18);
		courseDto.setId(1L);

		when(courseService.getSortedCourses(0)).thenReturn(retVal);
		when(modelMapper.map(c, CourseDto.class)).thenReturn(courseDto);

		MvcResult mvcResult = mockMvc.perform(get("/rest/SortedCourses/0")).andExpect(status().isOk()).andReturn();
		
		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		objectMapper.setSerializationInclusion(Include.NON_EMPTY);

		assertThat(actualResponseBody)
				.isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(retVal));
	}
	
	@DisplayName("Move course to history")
	@Test
	void moveCourseToHistory_whenValidInput_thenReturns200() throws Exception {

		doNothing().when(courseService).moveToHistoryCurse(1L);
		mockMvc.perform(post("/rest/MoveToHistoryCourse/1")).andExpect(status().isOk());
	}


	@DisplayName("Should add tee with correct result")
	@Test
	void addTeeWhenValidInputThenReturns200() throws Exception {

		var input = new CourseTeeDto();
		input.setTee("Ladies red");
		input.setCr(64.6F);
		input.setSr(123);
		input.setSex(false);
		input.setTeeType(0);

		doNothing().when(courseService).addTee(any(), any());

		mockMvc.perform(post("/rest/Tee/1").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(input))).andExpect(status().isOk()).andReturn();
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
