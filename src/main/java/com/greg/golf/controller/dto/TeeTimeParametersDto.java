package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.*;

@Getter
@Setter
public class TeeTimeParametersDto {

	@NotNull
	@Size(min = 5, max = 5, message = "tee time must be 5 characters long")
	@Schema(description = "tee time", example = "09:00", accessMode = READ_WRITE, maxLength = 5)
	private String firstTeeTime;

	@NotNull
	@Min(value = 8)
	@Max(value = 12)
	@Schema(description = "tee time step in minutes", example = "8", accessMode = READ_WRITE, minimum = "8", maximum = "12")
	private Integer teeTimeStep;

	@NotNull
	@Min(value = 2)
	@Max(value = 4)
	@Schema(description = "flight size", example = "4", accessMode = READ_WRITE, minimum = "2", maximum = "4")
	private Integer flightSize;

	@NotNull
	@JsonProperty(value = "teeTimes", access = JsonProperty.Access.READ_WRITE)
	@Schema(description = "List of teeTimes objects", accessMode = READ_WRITE)
	private List<TeeTimeDto> teeTimes;

	@ToString.Exclude
	@Schema(description = "Publish status: false - not, true - yes", example = "false", accessMode = READ_WRITE)
	private Boolean published;
}
