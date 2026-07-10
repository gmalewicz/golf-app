package com.greg.golf.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import lombok.Setter;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Getter
@Setter
public class PlayerRoundDto {

	@Schema(description = "Player identifier", example = "25", accessMode = READ_ONLY)
	private Long playerId;

	@Schema(description = "Player handicap", example = "38.5", accessMode = READ_ONLY, minimum = "-5", maximum = "54")
	private Float whs;

	@Schema(description = "Tee identifier", example = "21", accessMode = READ_ONLY)
	private Long teeId;

	@Schema(description = "Course rate", example = "71", accessMode = READ_ONLY, minimum = "30", maximum = "89")
	private Float cr;

	@Schema(description = "Slope rate", example = "78", accessMode = READ_ONLY, minimum = "55", maximum = "155")
	private Integer sr;

	@Schema(description = "Tee type: 0 - 18 holes, 1 - first 9, 2 - second 9", example = "0", accessMode = READ_ONLY, minimum = "1", maximum = "2")
	private Integer teeType;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "Team assignment: 1 - team 1, 2 - team 2. Absent if not assigned.", example = "1", accessMode = READ_ONLY, allowableValues = {"1", "2"})
	private Integer team;
}
