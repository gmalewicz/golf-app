package com.greg.golf.error;

import lombok.Data;
import lombok.NonNull;

@Data
public class ApiErrorResponse {

	@NonNull
	private String error;
	@NonNull
    private String message;

}
