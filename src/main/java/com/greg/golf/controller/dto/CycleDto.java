package com.greg.golf.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;



import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;

@Getter
@Setter
public class CycleDto {

	@Schema(description = "Cycle identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Cycle name", example = "2018 Golf Cycle", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@NotNull
	@Schema(description = "Cycle status: false - open, true - close", example = "false", accessMode = READ_WRITE)
	private Boolean status;

	@Schema(description = "Cycle owner", accessMode = READ_WRITE)
	private PlayerDto player;

	@NotNull
	@Schema(description = "Number of best rounds from cycle to be included in the result, 0 - all rounds",
			example = "0", minimum = "0", maximum = "20", accessMode = READ_WRITE)
	private Integer bestRounds;

	@NotNull
	@Schema(description = "Maximum handicap", example = "38.5", accessMode = READ_WRITE, minimum="-5", maximum="54")
	private Float maxWhs;
}
