package com.greg.golf.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

import javax.mail.MessagingException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.greg.golf.entity.helpers.Views;

import com.greg.golf.controller.dto.GameDto;

import com.greg.golf.controller.dto.OnlineRoundDto;
import com.greg.golf.controller.dto.OnlineScoreCardDto;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.controller.dto.PlayerRoundDto;
import com.greg.golf.controller.dto.RoundDto;
import com.greg.golf.controller.dto.ScoreCardDto;
import com.greg.golf.controller.dto.TournamentDto;
import com.greg.golf.controller.dto.TournamentResultDto;

import com.greg.golf.entity.Game;
import com.greg.golf.entity.OnlineRound;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.entity.Tournament;
import com.greg.golf.entity.TournamentRound;
import com.greg.golf.error.SendingMailFailureException;

import com.greg.golf.service.GameService;
import com.greg.golf.service.OnlineRoundService;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;
import com.greg.golf.service.TournamentService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Course API"), @Tag(name = "Round API"), @Tag(name = "Game API"),
		@Tag(name = "Tournament API") })
public class GolfRESTController {

	@Autowired
	private RoundService roundService;

	@Autowired
	private OnlineRoundService onlineRoundService;

	@Autowired
	private GameService gameService;

	@Autowired
	private ScoreCardService scoreCardService;

	@Autowired
	private TournamentService tournamentService;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private ModelMapper modelMapper;

	@Tag(name = "Round API")
	@Operation(summary = "Add the new round for a player.")
	@PostMapping(value = "/rest/Round")
	public HttpStatus addRound(
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		Round round = modelMapper.map(roundDto, Round.class);
		roundService.saveRound(round);

		return HttpStatus.OK;
	}

	@Tag(name = "Round API")
	@JsonView(Views.RoundWithoutPlayer.class)
	@Operation(summary = "Get round for player id.")
	@GetMapping(value = "/rest/Rounds/{playerId}/{pageId}")
	public List<RoundDto> getRound(
			@Parameter(description = "Player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") Integer pageId) {

		log.info("Requested list of round for Player id -  " + playerId + " and page id " + pageId);

		Player player = new Player();
		player.setId(playerId);

		return mapList(roundService.listByPlayerPageable(player, pageId), RoundDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Get recent rounds")
	@GetMapping(value = "/rest/RecentRounds/{pageId}")
	public List<RoundDto> getRecentRounds(
			@Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") Integer pageId) {

		log.info("Requested list of recent rounds for page id " + pageId);

		return mapList(roundService.getRecentRounds(pageId), RoundDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Gets score cards for round id.")
	@GetMapping(value = "/rest/ScoreCard/{id}")
	public List<ScoreCardDto> getScoreCards(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("id") Long id) {

		log.info("Requested list of scorecards for Round id -  " + id);

		Round round = new Round();
		round.setId(id);

		return mapList(scoreCardService.listByRound(round), ScoreCardDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Deletes round with given id.")
	@DeleteMapping("/rest/Round/{id}")
	public ResponseEntity<Long> deleteRound(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to delete round: " + id);

		try {
			roundService.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(id, HttpStatus.OK);

	}

	@Tag(name = "Game API")
	@Operation(summary = "Add Game")
	@PostMapping(value = "/rest/Game")
	public HttpStatus addGame(@Parameter(description = "Game object", required = true) @RequestBody GameDto gameDto) {

		log.info("trying to add course: " + gameDto);

		gameService.save(modelMapper.map(gameDto, Game.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Game API")
	@Operation(summary = "Return games for player id")
	@GetMapping(value = "/rest/Game/{id}")
	public List<GameDto> getGames(
			@Parameter(description = "Player id", example = "1", required = true) @PathVariable("id") Long id) {

		log.info("Requested list of games for Player id -  " + id);

		Player player = new Player();
		player.setId(id);

		return mapList(gameService.listByPlayer(player), GameDto.class);
	}

	@Tag(name = "Game API")
	@Operation(summary = "Sends game details for email address")
	@PostMapping(value = "/rest/SendGame")
	// @ApiResponse(responseCode = "500", description="Failed to send an email")
	public HttpStatus sendGame(
			@Parameter(description = "GameSendData object", required = true) @RequestBody com.greg.golf.controller.dto.GameSendData gameSendData) {

		log.info("trying to send email with game details: " + gameSendData);

		try {
			gameService.sendGameDetail(gameSendData);
		} catch (MessagingException | NoSuchElementException e) {
			log.error("Failed sending game data for: " + gameSendData.getEmail());
			throw new SendingMailFailureException();
		}

		return HttpStatus.OK;
	}

	@Tag(name = "Round API")
	@Operation(summary = "Deletes score card for given player id and round id")
	@DeleteMapping("/rest/ScoreCard/{playerId}/{roundId}")
	public HttpStatus deleteScoreCard(
			@Parameter(description = "player id", example = "1", required = true) @PathVariable Long playerId,
			@Parameter(description = "round id", example = "1", required = true) @PathVariable Long roundId) {

		log.debug("trying to delete scorecard for round: " + roundId + " and player " + playerId);

		roundService.deleteScorecard(playerId, roundId);

		log.info("scorecard deleted for round: " + roundId + " and player " + playerId);

		return HttpStatus.OK;

	}

	@Tag(name = "Round API")
	@Operation(summary = "Updates score card for given round.")
	@PatchMapping("/rest/ScoreCard")
	public HttpStatus updateRound(
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		log.debug("trying to update round: " + roundDto.getId());

		roundService.updateScoreCard(modelMapper.map(roundDto, Round.class));

		log.info("round: " + roundDto.getId() + " updated");

		return HttpStatus.OK;

	}

	@Tag(name = "Round API")
	@Operation(summary = "Return data required for course handicap calculation")
	@GetMapping(value = "/rest/RoundPlayerDetails/{playerId}/{roundId}")
	public PlayerRoundDto getRoundPlayerDetails(
			@Parameter(description = "Player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("roundId") Long roundId) {

		log.info("Requested round details for Player id - " + playerId + " and round id " + roundId);

		return modelMapper.map(roundService.getForPlayerRoundDetails(playerId, roundId), PlayerRoundDto.class);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all tournaments")
	@GetMapping(value = "/rest/Tournament")
	public List<TournamentDto> getTournaments() {
		log.info("Requested list of tournaments");

		return mapList(tournamentService.findAllTournamnets(), TournamentDto.class);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all tournament results")
	@GetMapping(value = "/rest/TournamentResult/{tournamentId}")
	public List<TournamentResultDto> getTournamentResult(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested all tournament results sorted by played round desc and score netto ascending");

		Tournament tournament = new Tournament();
		tournament.setId(tournamentId);

		return mapList(tournamentService.findAllTournamnetsResults(tournament), TournamentResultDto.class);

	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Return all rounds that can be added to tournament")
	@JsonView(Views.RoundWithoutPlayer.class)
	@GetMapping(value = "/rest/TournamentRounds/{tournamentId}")
	public List<RoundDto> getTournamentRounds(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId) {
		log.info("Requested rounds for tournament");
		return mapList(tournamentService.getAllPossibleRoundsForTournament(tournamentId), RoundDto.class);
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Adds round to tournament")
	@PostMapping(value = "/rest/TournamentRound/{tournamentId}")
	// @ApiResponse(responseCode = "500", description="Failed to send an email")
	public HttpStatus addRoundToTournament(
			@Parameter(description = "Tournamnet id", example = "1", required = true) @PathVariable("tournamentId") Long tournamentId,
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		log.info("trying to add round to tournament: " + roundDto);

		tournamentService.addRound(tournamentId, modelMapper.map(roundDto, Round.class), true);
		log.info("Round added");

		return HttpStatus.OK;
	}

	@Tag(name = "Tournament API")
	@Operation(summary = "Adds tournament")
	@PostMapping(value = "/rest/Tournament")
	public HttpStatus addTournament(
			@Parameter(description = "Tournament object", required = true) @RequestBody TournamentDto tournamentDto) {

		log.info("trying to add tournament: " + tournamentDto);

		tournamentService.addTournamnet(modelMapper.map(tournamentDto, Tournament.class));

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

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Adds online rounds")
	@PostMapping(value = "/rest/OnlineRounds")
	public List<OnlineRoundDto> addOnlineRounds(
			@Parameter(description = "List of OnlineRound objects", required = true) @RequestBody List<OnlineRoundDto> onlineRounds) {

		log.info("trying to add onlineRounds");
		List<OnlineRound> orLst = mapList(onlineRounds, OnlineRound.class);

		return mapList(onlineRoundService.save(orLst), OnlineRoundDto.class);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Adds online round")
	@PostMapping(value = "/rest/OnlineRound")
	public OnlineRoundDto addOnlineRound(
			@Parameter(description = "OnlineRound object", required = true) @RequestBody OnlineRoundDto onlineRoundDto) {

		log.info("trying to add onlineRound: " + onlineRoundDto);
		OnlineRound or = modelMapper.map(onlineRoundDto, OnlineRound.class);

		return modelMapper.map(onlineRoundService.save(or), OnlineRoundDto.class);

	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return all online rounds")
	@GetMapping(value = "/rest/OnlineRound")
	public List<OnlineRoundDto> getOnlineRounds() {
		log.info("Requested online rounds");
		return mapList(onlineRoundService.getOnlineRounds(), OnlineRoundDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return all score cards already saved for on-line round")
	@GetMapping(value = "/rest/OnlineScoreCard/{onlineRoundId}")
	public List<OnlineScoreCardDto> getOnlineScoreCards(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable("onlineRoundId") Long onlineRoundId) {
		log.info("Requested online round score cards");
		return mapList(onlineRoundService.getOnlineScoreCards(onlineRoundId), OnlineScoreCardDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Delete online round with given id.")
	@DeleteMapping("/rest/OnlineRound/{id}")
	public ResponseEntity<Long> deleteOnlineRound(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to delete online round: " + id);

		try {
			onlineRoundService.delete(id);
		} catch (Exception e) {
			return new ResponseEntity<>(id, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(id, HttpStatus.OK);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Finalize online round")
	@PostMapping(value = "/rest/FinalizeOnlineRound/{id}")
	public Round finalizeOnlineRound(
			@Parameter(description = "Online round id", example = "1", required = true) @PathVariable Long id) {

		log.info("trying to finzalie online round: " + id);

		return onlineRoundService.finalizeById(id);

	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return online rounds for course")
	@GetMapping(value = "/rest/OnlineRoundCourse/{courseId}")
	public List<OnlineRoundDto> getOnlineRoundsCourse(
			@Parameter(description = "Course id", example = "1", required = true) @PathVariable("courseId") Long courseId) {
		log.info("Requested online rounds for course");
		return mapList(onlineRoundService.getOnlineRoundsForCourse(courseId), OnlineRoundDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return player for nick")
	@GetMapping(value = "/rest/Player/{nick}")
	public PlayerDto getPlayer(
			@Parameter(description = "nick", example = "player", required = true) @PathVariable("nick") String nick) {
		log.info("Requested player for nick");
		Optional<Player> player = playerService.getPlayer(nick);

		if (player.isEmpty()) {
			return null;
		}

		return modelMapper.map(player.get(), PlayerDto.class);
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Delete online round with given owner id.")
	@DeleteMapping("/rest/OnlineRoundForOwner/{ownerId}")
	public ResponseEntity<Long> deleteOnlineRoundForOwner(
			@Parameter(description = "Online round owner id", example = "1", required = true) @PathVariable Long ownerId) {

		log.info("trying to delete online round for owner: " + ownerId);

		try {
			onlineRoundService.deleteForOwner(ownerId);
		} catch (Exception e) {
			return new ResponseEntity<>(ownerId, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(ownerId, HttpStatus.OK);

	}

	@Tag(name = "Online scoreard API")
	@Operation(summary = "Finalize online rounds for owner")
	@PostMapping(value = "/rest/FinalizeOnlineOwnerRounds/")
	public HttpStatus finalizeOwnerOnlineRounds(
			@Parameter(description = "Owner object", required = true) @RequestBody Long ownerId) {

		log.info("trying to finalize online round for owner: " + ownerId);

		onlineRoundService.finalizeForOwner(ownerId);

		return HttpStatus.OK;
	}

	@Tag(name = "Online scorecard API")
	@Operation(summary = "Return online rounds for owner")
	@GetMapping(value = "/rest/OnlineRoundOwner/{ownerId}")
	public List<OnlineRoundDto> getOnlineRoundsOwner(
			@Parameter(description = "Player (owner) id", example = "1", required = true) @PathVariable("ownerId") Long ownerId) {
		log.info("Requested online rounds for owner");
		return mapList(onlineRoundService.getOnlineRoundsForOwner(ownerId), OnlineRoundDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Return data required for course handicap calculation")
	@GetMapping(value = "/rest/RoundPlayersDetails/{roundId}")
	public List<PlayerRoundDto> getPlayersDetailsForRound(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("roundId") Long roundId) {

		log.info("Requested players round details for round id " + roundId);

		return mapList(roundService.getForPlayerRoundDetails(roundId), PlayerRoundDto.class);
	}

	private <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
		return source.stream().map(element -> modelMapper.map(element, targetClass)).collect(Collectors.toList());
	}

}
