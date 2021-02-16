package com.greg.golf.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greg.golf.controller.dto.CourseDto;
import com.greg.golf.controller.dto.CourseNameDto;
import com.greg.golf.entity.Course;
import com.greg.golf.security.JwtAuthenticationEntryPoint;
import com.greg.golf.security.JwtRequestFilter;

import com.greg.golf.service.CourseService;
import com.greg.golf.service.PlayerService;

import lombok.extern.log4j.Log4j2;

import static org.mockito.BDDMockito.*;

@Log4j2
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = CourseController.class)
class GolfRESTControllerMockTest {

	@Autowired
	private MockMvc mockMvc;

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

	@Autowired
	ObjectMapper objectMapper;

	@BeforeAll
	public static void setup() {

		log.info("Set up completed");
	}

	@Test
	void whenValidInput_thenReturns200() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(courseNameDto))).andExpect(status().isOk());
	}

	@Test
	void whenNullValue_thenReturns400() throws Exception {
		CourseNameDto courseNameDto = new CourseNameDto(null);

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(courseNameDto))).andExpect(status().isBadRequest());

	}

	@Test
	void whenValidInput_thenMapsToBusinessModel() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");

		mockMvc.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
				.content(objectMapper.writeValueAsString(courseNameDto))).andExpect(status().isOk());

		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(courseService, times(1)).searchForCourses(stringCaptor.capture());
		assertThat(stringCaptor.getValue()).isEqualTo("Test");
	}

	@Test
	void whenValidInput_thenReturnsCourseList() throws Exception {

		CourseNameDto courseNameDto = new CourseNameDto("Test");
		Course c = new Course();
		c.setName("Test");
		c.setPar(72);
		c.setHoleNbr(18);
		c.setId(1l);

		List<Course> retVal = new ArrayList<>();
		retVal.add(c);

		CourseDto courseDto = new CourseDto();
		courseDto.setName("Test");
		courseDto.setPar(72);
		courseDto.setHoleNbr(18);
		courseDto.setId(1l);
		
		List<Course> expectedResponseBody = retVal;

		when(courseService.searchForCourses("Test")).thenReturn(retVal);
		when(modelMapper.map(c, CourseDto.class)).thenReturn(courseDto);
		
		MvcResult mvcResult = mockMvc
				.perform(post("/rest/SearchForCourse").contentType("application/json").characterEncoding("utf-8")
						.content(objectMapper.writeValueAsString(courseNameDto)))
				.andExpect(status().isOk()).andReturn();
		
		String actualResponseBody = mvcResult.getResponse().getContentAsString();

		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
	
		assertThat(actualResponseBody)
				.isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(expectedResponseBody));
	}

	@AfterAll
	public static void done() {

		log.info("Clean up completed");

	}

}
