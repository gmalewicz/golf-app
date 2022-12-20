package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY;

@Getter
@Setter
public class PlayerUpdateDto {

	@NotNull
	@Schema(description = "Player identifier", example = "25", accessMode = WRITE_ONLY)
	private Long id;

	@Min(value = -5)
	@Max(value = 54)
	@Schema(description = "Player handicap", example = "38.5", accessMode = WRITE_ONLY, minimum="-5", maximum="54")
	private Float whs;

	@Size(min = 1, max = 60, message = "Password must be between 1 and 60 characters")
	@Schema(description = "Player password", example = "welcome", accessMode = WRITE_ONLY, minLength=1, maxLength=60)
	protected String password;
}
