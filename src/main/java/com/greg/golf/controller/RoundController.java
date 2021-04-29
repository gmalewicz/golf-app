package com.greg.golf.controller;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.controller.dto.LimitedRoundDto;
import com.greg.golf.controller.dto.LimitedRoundWithPlayersDto;
import com.greg.golf.controller.dto.PlayerRoundDto;
import com.greg.golf.controller.dto.RoundDto;
import com.greg.golf.controller.dto.ScoreCardDto;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Round API") })
public class RoundController extends BaseController {

	private final RoundService roundService;
	private final ScoreCardService scoreCardService;

	public RoundController(ModelMapper modelMapper, RoundService roundService, ScoreCardService scoreCardService) {
		super(modelMapper);
		this.roundService = roundService;
		this.scoreCardService = scoreCardService;
	}

	@Tag(name = "Round API")
	@Operation(summary = "Add the new round for a player.")
	@PostMapping(value = "/rest/Round")
	public HttpStatus addRound(
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		var round = modelMapper.map(roundDto, Round.class);
		log.debug(round.getCourse().getTees());
		log.debug(round.getCourse());
		log.debug(roundDto.getCourse().getTees());
		log.debug(roundDto.getCourse());
		roundService.saveRound(round);

		return HttpStatus.OK;
	}

	@Tag(name = "Round API")
	@Operation(summary = "Get round for player id.")
	@GetMapping(value = "/rest/Rounds/{playerId}/{pageId}")
	public List<LimitedRoundDto> getRound(
			@Parameter(description = "Player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") Integer pageId) {

		log.info("Requested list of round for Player id -  " + playerId + " and page id " + pageId);

		var player = new Player();
		player.setId(playerId);

		return mapList(roundService.listByPlayerPageable(player, pageId), LimitedRoundDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Get recent rounds")
	@GetMapping(value = "/rest/RecentRounds/{pageId}")
	public List<LimitedRoundWithPlayersDto> getRecentRounds(
			@Parameter(description = "Page id", example = "0", required = true) @PathVariable("pageId") Integer pageId) {

		log.info("Requested list of recent rounds for page id " + pageId);

		return mapList(roundService.getRecentRounds(pageId), LimitedRoundWithPlayersDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Gets score cards for round id.")
	@GetMapping(value = "/rest/ScoreCard/{id}")
	public List<ScoreCardDto> getScoreCards(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("id") Long id) {

		log.info("Requested list of scorecards for Round id -  " + id);

		var round = new Round();
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

	@Tag(name = "Round API")
	@Operation(summary = "Return data required for course handicap calculation")
	@GetMapping(value = "/rest/RoundPlayersDetails/{roundId}")
	public List<PlayerRoundDto> getPlayersDetailsForRound(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("roundId") Long roundId) {

		log.info("Requested players round details for round id " + roundId);

		return mapList(roundService.getByRoundId(roundId), PlayerRoundDto.class);
	}
}
