package com.greg.golf.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_WRITE;
import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TournamentDto {

	@Schema(description = "Tournament identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Tournament name", example = "Test tournament", accessMode = READ_WRITE, maxLength = 255)
	private String name;

	@NotNull
	@Schema(description = "Tournament start date", accessMode = READ_WRITE, pattern = "yyyy/MM/dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date startDate;

	@NotNull
	@Schema(description = "Tournament end date", accessMode = READ_WRITE, pattern = "yyyy/MM/dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
	private Date endDate;

	@NotNull
	@Schema(description = "Number of best rounds from tournament to be included in the result, 0 - all rounds",
			example = "0", minimum = "0", maximum = "10", accessMode = READ_WRITE)
	private Integer bestRounds;

	@Schema(description = "Tournament owner", accessMode = READ_WRITE)
	private PlayerDto player;
}
