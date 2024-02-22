package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class TeeTimeDto {

	@NotNull
	@Min(value = 1)
	@Max(value = 20)
	@Schema(description = "flight number", example = "1", accessMode = READ_WRITE, minimum = "1", maximum = "20")
	private Integer flight;

	@NotNull
	@Size(min = 5, max = 5, message = "tee time must be 5 characters long")
	@Schema(description = "tee time", example = "09:00", accessMode = READ_WRITE, maxLength = 5)
	private String time;

	@Size(min = 1, max = 20, message = "Nick must be between 1 and 20 characters")
	@Schema(description = "Player nick name", example = "golfer", accessMode = READ_ONLY, maxLength=20)
	private String nick;

	@Min(value = -5)
	@Max(value = 54)
	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_ONLY, minimum = "-5", maximum = "54")
	private Float hcp;
}
