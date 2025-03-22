package com.greg.golf.controller;

import com.greg.golf.error.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.NoSuchElementException;

@Slf4j
@SpringBootTest
class ApiExceptionHandlerTest {

	@Autowired
	private ApiExceptionHandler apiExceptionHandler;

	@DisplayName("Should throw TooManyPlayersException")
	@Test
	void TooManyPlayersExceptionTest()  {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new TooManyPlayersException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw PlayerAlreadyHasThatRoundException")
	@Test
	void PlayerAlreadyHasThatRoundExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new PlayerAlreadyHasThatRoundException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw PlayerNickInUseException")
	@Test
	void PlayerNickInUseExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new PlayerNickInUseException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw ConstraintViolationException")
	@Test
	void ConstraintViolationExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new ConstraintViolationException("Test", null));
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw SendingMailFailureException")
	@Test
	void SendingMailFailureExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new SendingMailFailureException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw NoSuchElementException")
	@Test
	void NoSuchElementExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new NoSuchElementException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw TooFewHolesForTournamentException")
	@Test
	void TooFewHolesForTournamentExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new TooFewHolesForTournamentException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw ReCaptchaInvalidException")
	@Test
	void ReCaptchaInvalidExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new ReCaptchaInvalidException("Test"));
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw ReCaptchaUnavailableException")
	@Test
	void ReCaptchaUnavailableExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new ReCaptchaUnavailableException("Test"));
		Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@DisplayName("Should throw UnauthorizedException")
	@Test
	void UnauthorizedExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new UnauthorizedException());
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@DisplayName("Should throw EntityNotFoundException")
	@Test
	void EntityNotFoundExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new EntityNotFoundException());
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw BadCredentialsException")
	@Test
	void BadCredentialsExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new BadCredentialsException("Test"));
		Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@DisplayName("Should throw TooShortStringForSearchException")
	@Test
	void TooShortStringForSearchExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new TooShortStringForSearchException());
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw IllegalArgumentException")
	@Test
	void IllegalArgumentExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new IllegalArgumentException());
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw InvalidDataAccessApiUsageException")
	@Test
	void InvalidDataAccessApiUsageExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new InvalidDataAccessApiUsageException("Test"));
		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@DisplayName("Should throw ScoreCardUpdateException")
	@Test
	void ScoreCardUpdateExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new ScoreCardUpdateException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw DeleteTournamentPlayerException")
	@Test
	void DeleteTournamentPlayerExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new DeleteTournamentPlayerException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw DuplicatePlayerInTournamentException")
	@Test
	void DuplicatePlayerInTournamentExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new DuplicatePlayerInTournamentException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw DuplicatePlayerInLeagueException")
	@Test
	void DuplicatePlayerInLeagueExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new DuplicatePlayerInLeagueException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw DuplicateMatchInLeagueException")
	@Test
	void DuplicateMatchInLeagueExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new DuplicateMatchInLeagueException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw MatchResultForNotLeaguePlayerException")
	@Test
	void MatchResultForNotLeaguePlayerExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new MatchResultForNotLeaguePlayerException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw PlayerHasMatchException")
	@Test
	void PlayerHasMatchExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new PlayerHasMatchException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw LeagueClosedException")
	@Test
	void LeagueClosedExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new LeagueClosedException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}

	@DisplayName("Should throw TeeAlreadyExistsException")
	@Test
	void TeeAlreadyExistsExceptionTest() {

		ResponseEntity<ApiErrorResponse>  response = apiExceptionHandler.handleApiException(new TeeAlreadyExistsException());
		Assertions.assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
	}
}
