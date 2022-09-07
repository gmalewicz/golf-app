package com.greg.golf.controller;

import com.greg.golf.controller.dto.CycleDto;
import com.greg.golf.controller.dto.CycleResultDto;
import com.greg.golf.controller.dto.CycleTournamentDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.service.CycleService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Cycle API") })
public class CycleController extends BaseController {

	private final CycleService cycleService;

	public CycleController(ModelMapper modelMapper, CycleService cycleService) {
		super(modelMapper);
		this.cycleService = cycleService;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Cycle API")
	@Operation(summary = "Adds cycle")
	@PostMapping(value = "/rest/Cycle")
	public HttpStatus addCycle(
			@Parameter(description = "Cycle object", required = true) @RequestBody CycleDto cycleDto) {

		log.info("trying to add cycle: " + cycleDto.getName());

		cycleService.addCycle(modelMapper.map(cycleDto, Cycle.class));

		return HttpStatus.OK;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Cycle API")
	@Operation(summary = "Adds cycle tournament")
	@PostMapping(value = "/rest/CycleTournament")
	public HttpStatus addCycleTournament(
			@Parameter(description = "CycleTournament object", required = true) @RequestBody CycleTournamentDto cycleTournamentDto) {

		log.info("trying to add cycle tournament: " + cycleTournamentDto.getName());

		cycleService.addCycleTournament(modelMapper.map(cycleTournamentDto, CycleTournament.class), cycleTournamentDto.getTournamentResult());

		return HttpStatus.OK;
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Return all cycles")
	@GetMapping(value = "/rest/Cycle")
	public List<CycleDto> getCycles() {
		log.info("Requested list of cycles");

		return mapList(cycleService.findAllCycles(), CycleDto.class);
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Return all cycle Tournaments")
	@GetMapping(value = "/rest/CycleTournament/{cycleId}")
	public List<CycleTournamentDto> getCycleTournaments(
			@Parameter(description = "Cycle id", example = "1", required = true) @PathVariable("cycleId") Long cycleId) {
		log.info("Requested list of cycle tournaments for cycle: " + cycleId);
		return mapList(cycleService.findAllCycleTournaments(cycleId), CycleTournamentDto.class);
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Return cycle results")
	@GetMapping(value = "/rest/CycleResult/{cycleId}")
	public List<CycleResultDto> getCycleResults(
			@Parameter(description = "Cycle id", example = "1", required = true) @PathVariable("cycleId") Long cycleId) {
		log.info("Requested cycle results for cycle: " + cycleId);
		return mapList(cycleService.findCycleResults(cycleId), CycleResultDto.class);
	}

	@SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
	@Tag(name = "Cycle API")
	@Operation(summary = "Close cycle. Further updates will not be possible.")
	@PatchMapping(value = "/rest/CycleClose/{cycleId}")
	public HttpStatus resetPassword(
			@Parameter(description = "Cycle id to be closed", required = true) @PathVariable("cycleId") Long cycleId) {

		log.info("trying to close cycle: " + cycleId);

		cycleService.closeCycle(cycleId);

		return HttpStatus.OK;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Cycle API")
	@Operation(summary = "Deletes the last cycle tournament")
	@PostMapping(value = "/rest/DeleteCycleTournament")
	public HttpStatus deleteCycleTournament(
			@Parameter(description = "Cycle object", required = true) @RequestBody CycleDto cycleDto) {

		log.info("trying to delete tournament for cycle: " + cycleDto.getName());

		cycleService.removeLastCycleTournament(modelMapper.map(cycleDto, Cycle.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Cycle API")
	@Operation(summary = "Deletes the cycle")
	@DeleteMapping(value = "/rest/Cycle/{cycleId}")
	public HttpStatus deleteCycle(
			@Parameter(description = "Cycle id to be deleted", required = true) @PathVariable("cycleId") Long cycleId) {

		log.info("trying to delete cycle: " + cycleId);

		cycleService.deleteCycle(cycleId);

		return HttpStatus.OK;
	}
}
