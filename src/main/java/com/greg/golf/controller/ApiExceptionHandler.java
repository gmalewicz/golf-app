package com.greg.golf.controller;

import java.util.Locale;
import java.util.NoSuchElementException;

import com.greg.golf.error.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	private final MessageSource messageSource;

	public ApiExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(TooManyPlayersException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooManyPlayersException ex) {

		String message = getLocalizedMessage("error-1", null);
		var response = new ApiErrorResponse("1", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(PlayerAlreadyHasThatRoundException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerAlreadyHasThatRoundException ex) {

		String message = getLocalizedMessage("error-2", null);
		var response = new ApiErrorResponse("2", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(PlayerNickInUseException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerNickInUseException ex) {
		String message = getLocalizedMessage("error-3", null);
		var response = new ApiErrorResponse("3", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ConstraintViolationException ex) {

		String message = getLocalizedMessage("error-4", null);
		var response = new ApiErrorResponse("4", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(SendingMailFailureException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(SendingMailFailureException ex) {

		String message = getLocalizedMessage("error-5", null);
		var response = new ApiErrorResponse("5", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(NoSuchElementException ex) {

		String message = getLocalizedMessage("error-6", null);
		var response = new ApiErrorResponse("6", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(TooFewHolesForTournamentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooFewHolesForTournamentException ex) {

		String message = getLocalizedMessage("error-8", null);
		var response = new ApiErrorResponse("8", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ReCaptchaInvalidException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ReCaptchaInvalidException ex) {
		
		var response = new ApiErrorResponse("9", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(ReCaptchaUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ReCaptchaUnavailableException ex) {
		
		var response = new ApiErrorResponse("10", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(UnauthorizedException ex) {

		String message = getLocalizedMessage("error-11", null);
		var response = new ApiErrorResponse("11", message);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(EntityNotFoundException ex) {

		String message = getLocalizedMessage("error-12", null);
		var response = new ApiErrorResponse("12", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		
		var errors = new StringBuilder();
	    ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
			errors.append(fieldName);
			errors.append(" - ");
	        errors.append(errorMessage);
	        errors.append(" ");
	    });

		String message = getLocalizedMessage("error-13", new Object[]{errors});
		var response = new ApiErrorResponse("13", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(BadCredentialsException ex) {

		String message = getLocalizedMessage("error-14", null);
		var response = new ApiErrorResponse("14", message);
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(TooShortStringForSearchException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TooShortStringForSearchException ex) {

		String message = getLocalizedMessage("error-15", null);
		var response = new ApiErrorResponse("15", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(IllegalArgumentException ex) {

		String message = getLocalizedMessage("error-16", null);
		var response = new ApiErrorResponse("16", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(InvalidDataAccessApiUsageException ex) {

		String message = getLocalizedMessage("error-17", null);
		var response = new ApiErrorResponse("17", message);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(ScoreCardUpdateException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ScoreCardUpdateException ex) {

		String message = getLocalizedMessage("error-18", null);
		var response = new ApiErrorResponse("18", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DeleteTournamentPlayerException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DeleteTournamentPlayerException ex) {

		String message = getLocalizedMessage("error-19", null);
		var response = new ApiErrorResponse("19", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicatePlayerInTournamentException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicatePlayerInTournamentException ex) {

		String message = getLocalizedMessage("error-20", null);
		var response = new ApiErrorResponse("20", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicatePlayerInLeagueException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicatePlayerInLeagueException ex) {

		String message = getLocalizedMessage("error-21", null);
		var response = new ApiErrorResponse("21", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(DuplicateMatchInLeagueException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(DuplicateMatchInLeagueException ex) {

		String message = getLocalizedMessage("error-22", null);
		var response = new ApiErrorResponse("22", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( MatchResultForNotLeaguePlayerException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(MatchResultForNotLeaguePlayerException ex) {

		String message = getLocalizedMessage("error-23", null);
		var response = new ApiErrorResponse("23", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( PlayerHasMatchException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(PlayerHasMatchException ex) {

		String message = getLocalizedMessage("error-24", null);
		var response = new ApiErrorResponse("24", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( LeagueClosedException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(LeagueClosedException ex) {

		String message = getLocalizedMessage("error-25", null);
		var response = new ApiErrorResponse("25", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler( TeeAlreadyExistsException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(TeeAlreadyExistsException ex) {

		String message = getLocalizedMessage("error-26", null);
		var response = new ApiErrorResponse("26", message);
		return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
	}

	private String getLocalizedMessage(String translationKey, Object[] args)
	{
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(translationKey, args, locale);
	}
}
