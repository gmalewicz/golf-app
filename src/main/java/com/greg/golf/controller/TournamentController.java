package com.greg.golf.controller;

import java.util.List;

import com.greg.golf.controller.dto.*;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.TournamentPlayer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested all tournament results sorted by played round desc and score net ascending");

		return mapList(tournamentService.findAllTournamentsResults(tournamentId), TournamentResultDto.class);

	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all rounds that can be added to tournament")
	@GetMapping(value = "/rest/TournamentRounds/{tournamentId}")
	public List<LimitedRoundWithPlayersDto> getTournamentRounds(
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested rounds for tournament");
		return mapList(tournamentService.getAllPossibleRoundsForTournament(tournamentId), LimitedRoundWithPlayersDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Tournament API")
	@Operation(summary = "Adds round to tournament")
	@PostMapping(value = "/rest/TournamentRound/{tournamentId}")
	public HttpStatus addRoundToTournament(
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId,
			@Parameter(description = "Round object", required = true) @RequestBody LimitedRoundDto limitedRoundDto) {

		log.info("trying to add round to tournament: " + limitedRoundDto);
		tournamentService.addRound(tournamentId, limitedRoundDto.getId(), true);
		log.info("Round added");

		return HttpStatus.OK;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Tournament API")
	@Operation(summary = "Add tournament")
	@PostMapping(value = "/rest/Tournament")
	public HttpStatus addTournament(
			@Parameter(description = "Tournament object", required = true) @RequestBody @Valid TournamentDto tournamentDto) {

		log.info("trying to add tournament: " + tournamentDto);

		tournamentService.addTournament(modelMapper.map(tournamentDto, Tournament.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all rounds for a player belonging to the tournament")
	@GetMapping(value = "/rest/TournamentResultRound/{resultId}")
	public List<TournamentRound> getResultRounds(
			@Parameter(description = "Tournament result id", example = "1", required = true) @PathVariable("resultId") Long resultId) {
		log.info("Requested round details for tournament");
		return tournamentService.getTournamentRoundsForResult(resultId);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Adds round on behalf of the player to tournament")
	@PostMapping(value = "/rest/TournamentRoundOnBehalf/{tournamentId}")
	// @ApiResponse(responseCode = "500", description="Failed to send an email")
	public TournamentRound addRoundOnBehalf(
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId,
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		log.info("trying to add round on behalf to tournament: " + tournamentId + " and round: " + roundDto);
		var tournamentRound = tournamentService.addRoundOnBehalf(tournamentId, modelMapper.map(roundDto, Round.class));
		log.debug("Round added");

		return tournamentRound;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Delete result from tournament")
	@DeleteMapping(value = "/rest/TournamentResult/{tournamentResultId}")
	public HttpStatus deleteResult(
			@Parameter(description = "Tournament result id", example = "1", required = true) @PathVariable("tournamentResultId") Long tournamentResultId) {

		log.info("Delete result from tournament: " + tournamentResultId);
		tournamentService.deleteResult(tournamentResultId);

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Close tournament. Further updates will not be possible.")
	@PatchMapping(value = "/rest/TournamentClose/{tournamentId}")
	public HttpStatus closeTournament(
			@Parameter(description = "Tournament id to be closed", required = true) @PathVariable("tournamentId") Long tournamentId) {

		log.info("trying to close tournament: " + tournamentId);

		tournamentService.closeTournament(tournamentId);

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Delete tournament")
	@DeleteMapping(value = "/rest/Tournament/{tournamentId}")
	public HttpStatus deleteTournament(
			@Parameter(description = "Tournament id to be deleted", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {

		log.info("Delete tournament: " + tournamentId);
		tournamentService.deleteTournament(tournamentId);

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Add player participant to tournament")
	@PostMapping(value = "/rest/TournamentPlayer")
	public HttpStatus addPlayer(
			@Parameter(description = "TournamentPlayer object", required = true) @RequestBody @Valid TournamentPlayerDto tournamentPlayerDto) {

		log.info("trying to add tournament player: " + tournamentPlayerDto);

		tournamentService.addPlayer(modelMapper.map(tournamentPlayerDto, TournamentPlayer.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Delete player participant or all players from tournament")
	@DeleteMapping(value = {"/rest/TournamentPlayer/{tournamentId}/{playerId}", "/rest/TournamentPlayer/{tournamentId}"})
	public HttpStatus deletePlayer(
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId,
			@Parameter(description = "Player id", example = "1") @PathVariable(name = "playerId", required = false) Long playerId) {

		log.info("Delete tournament player: " + playerId + " for tournament " + tournamentId);

		if (playerId != null) {

			tournamentService.deletePlayer(tournamentId, playerId);
		} else {
			tournamentService.deletePlayers(tournamentId);
		}

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all players belonging to the tournament")
	@GetMapping(value = "/rest/TournamentPlayer/{tournamentId}")
	public List<TournamentPlayer> getTournamentPlayers(
			@Parameter(description = "Tournament id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested player participating in tournament " + tournamentId);
		return tournamentService.getTournamentPlayers(tournamentId);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Update player handicap")
	@PatchMapping(value = "/rest/TournamentPlayer")
	public HttpStatus updatePlayer(
			@Parameter(description = "TournamentPlayer object", required = true) @RequestBody @Valid TournamentPlayerDto tournamentPlayerDto) {

		log.info("trying to update tournament player whs: " + tournamentPlayerDto);

		tournamentService.updatePlayer(tournamentPlayerDto.getTournamentId(), tournamentPlayerDto.getPlayerId(), tournamentPlayerDto.getWhs());

		return HttpStatus.OK;
	}

}
