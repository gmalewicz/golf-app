package com.greg.golf.controller;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.controller.dto.LimitedRoundDto;
import com.greg.golf.controller.dto.LimitedRoundWithPlayersDto;
import com.greg.golf.controller.dto.TournamentDto;
import com.greg.golf.controller.dto.TournamentResultDto;

import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.service.TournamentService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Tournament API") })
public class TournamentController extends BaseController {

	private final TournamentService tournamentService;

	public TournamentController(ModelMapper modelMapper, TournamentService tournamentService) {
		super(modelMapper);
		this.tournamentService = tournamentService;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all tournaments")
	@GetMapping(value = "/rest/Tournament")
	public List<TournamentDto> getTournaments() {
		log.info("Requested list of tournaments");

		return mapList(tournamentService.findAllTournaments(), TournamentDto.class);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all tournament results")
	@GetMapping(value = "/rest/TournamentResult/{tournamentId}")
	public List<TournamentResultDto> getTournamentResult(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested all tournament results sorted by played round desc and score netto ascending");

		return mapList(tournamentService.findAllTournamentsResults(tournamentId), TournamentResultDto.class);

	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all rounds that can be added to tournament")
	@GetMapping(value = "/rest/TournamentRounds/{tournamentId}")
	public List<LimitedRoundWithPlayersDto> getTournamentRounds(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested rounds for tournament");
		return mapList(tournamentService.getAllPossibleRoundsForTournament(tournamentId), LimitedRoundWithPlayersDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Tournament API")
	@Operation(summary = "Adds round to tournament")
	@PostMapping(value = "/rest/TournamentRound/{tournamentId}")
	// @ApiResponse(responseCode = "500", description="Failed to send an email")
	public HttpStatus addRoundToTournament(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId,
			@Parameter(description = "Round object", required = true) @RequestBody LimitedRoundDto limitedRoundDto) {

		log.info("trying to add round to tournament: " + limitedRoundDto);
		tournamentService.addRound(tournamentId, limitedRoundDto.getId(), true);
		log.info("Round added");

		return HttpStatus.OK;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Tournament API")
	@Operation(summary = "Adds tournament")
	@PostMapping(value = "/rest/Tournament")
	public HttpStatus addTournament(
			@Parameter(description = "Tournament object", required = true) @RequestBody TournamentDto tournamentDto) {

		log.info("trying to add tournament: " + tournamentDto);

		tournamentService.addTournament(modelMapper.map(tournamentDto, Tournament.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all rounds for a player belonging to the tournament")
	@GetMapping(value = "/rest/TournamentResultRound/{resultId}")
	public List<TournamentRound> getResultRounds(
			@Parameter(description = "Tournamnet result id", example = "1", required = true) @PathVariable("resultId") Long resultId) {
		log.info("Requested round details for tournament");
		return tournamentService.getTournamentRoundsForResult(resultId);
	}
}
