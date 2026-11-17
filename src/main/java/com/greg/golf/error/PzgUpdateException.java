package com.greg.golf.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PzgUpdateException extends ResponseStatusException {

	public PzgUpdateException(HttpStatus status, String reason) {
		super(status, reason);
	}
}
