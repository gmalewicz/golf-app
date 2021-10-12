package com.greg.golf.controller;

import com.greg.golf.controller.dto.CycleDto;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.repository.CycleTournamentRepository;
import com.greg.golf.service.CycleService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Cycle API") })
public class CycleController extends BaseController {

	private final CycleService cycleService;

	public CycleController(ModelMapper modelMapper, CycleService cycleService) {
		super(modelMapper);
		this.cycleService = cycleService;
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Adds cycle")
	@PostMapping(value = "/rest/Cycle")
	public HttpStatus addCycle(
			@Parameter(description = "Cycle object", required = true) @RequestBody CycleDto cycleDto) {

		log.info("trying to add cycle: " + cycleDto);

		cycleService.addCycle(modelMapper.map(cycleDto, Cycle.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Adds cycle tournament")
	@PostMapping(value = "/rest/CycleTournament")
	public HttpStatus addCycleTournament(
			@Parameter(description = "CycleTournament object", required = true) @RequestBody CycleTournamentDto cycleTournamentDto) {

		log.info("trying to add cycle tournament: " + cycleTournamentDto);

		cycleService.addCycle(modelMapper.map(cycleTournamentDto, Cycle.class));

		return HttpStatus.OK;
	}
}
