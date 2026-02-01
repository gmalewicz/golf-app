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
public class BaseRoundDto {

	@Schema(description = "Round identifier", example = "25", accessMode = READ_ONLY)
	private Long id;

	@NotNull
	@Schema(description = "Supported formats: " +
			"0 = stroke play, " +
			"1 = match play, " +
			"2 = four ball stroke play, " +
			"3 = four ball stroke play",
			example = "0", accessMode = READ_WRITE, allowableValues = {"0", "1", "2", "3"})
	private Integer format;

	@Schema(description = "Match play allowance format", example = "0.5", accessMode = READ_ONLY, allowableValues = {
			"0.5", "0.75", "0.9", "1" })
	private Float mpFormat;

	@NotNull
	@Schema(description = "Date for a round", accessMode = READ_WRITE, pattern = "yyyy/MM/dd kk:mm")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd kk:mm")
	private Date roundDate;
}
