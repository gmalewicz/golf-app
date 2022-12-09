package com.greg.golf.controller;

import java.util.List;

import com.greg.golf.controller.dto.*;
import com.greg.golf.service.PlayerService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.Round;
import com.greg.golf.service.RoundService;
import com.greg.golf.service.ScoreCardService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Round API") })
public class RoundController extends BaseController {

	private final RoundService roundService;
	private final PlayerService playerService;
	private final ScoreCardService scoreCardService;

	public RoundController(ModelMapper modelMapper, RoundService roundService, ScoreCardService scoreCardService,
						   PlayerService playerService) {
		super(modelMapper);
		this.roundService = roundService;
		this.scoreCardService = scoreCardService;
		this.playerService = playerService;
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Round API")
	@Operation(summary = "Add the new round for a player.")
	@PostMapping(value = "/rest/Round")
	public HttpStatus addRound(
			@Parameter(description = "Round object", required = true) @RequestBody RoundDto roundDto) {

		log.info("Requested adding round for player id -  " + roundDto.getPlayer().first().getId());
		
		roundService.saveRound(modelMapper.map(roundDto, Round.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Round API")
	@Operation(summary = "Get the round for a given id.")
	@GetMapping(value = "/rest/Round/{roundId}")
	public LimitedRoundWithPlayersDto getRound(
			@Parameter(description = "Round identifier", required = true)  @PathVariable("roundId") Long roundId) {

		log.info("Get round for id -  " + roundId);

		return modelMapper.map(roundService.getWithPlayers(roundId).orElseThrow(), LimitedRoundWithPlayersDto.class);
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

	@SuppressWarnings("SameReturnValue")
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

	@SuppressWarnings("SameReturnValue")
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
	@Operation(summary = "Return data required for course handicap calculation for a player")
	@GetMapping(value = "/rest/RoundPlayerDetails/{playerId}/{roundId}")
	public PlayerRoundDto getRoundPlayerDetails(
			@Parameter(description = "Player id", example = "1", required = true) @PathVariable("playerId") Long playerId,
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("roundId") Long roundId) {

		log.info("Requested round details for Player id - " + playerId + " and round id " + roundId);

		return modelMapper.map(roundService.getForPlayerRoundDetails(playerId, roundId), PlayerRoundDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Return data required for course handicap calculation for all players")
	@GetMapping(value = "/rest/RoundPlayersDetails/{roundId}")
	public List<PlayerRoundDto> getPlayersDetailsForRound(
			@Parameter(description = "Round id", example = "1", required = true) @PathVariable("roundId") Long roundId) {

		log.info("Requested players round details for round id " + roundId);

		return mapList(roundService.getByRoundId(roundId), PlayerRoundDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Round API")
	@Operation(summary = "Updates whs for player for given round.")
	@PatchMapping("rest/UpdatePlayerRound")
	public HttpStatus updatePlayerRoundWHS(
			@Parameter(description = "Round WHS object", required = true) @RequestBody RoundWhsDto roundWhsDto) {

		roundService.updateRoundWhs(roundWhsDto.getPlayerId(), roundWhsDto.getRoundId(), roundWhsDto.getWhs());

		log.info("Round: " + roundWhsDto.getRoundId() + " for player " + roundWhsDto.getPlayerId() +
				" updated with whs " + roundWhsDto.getWhs());

		return HttpStatus.OK;
	}

	@SuppressWarnings("UnusedReturnValue")
	@Tag(name = "Round API")
	@Operation(summary = "Get player data")
	@GetMapping(value = "/rest/PlayerRoundCnt")
	public List<PlayerRoundCntDto> getPlayerList() {
		log.info("Requested player round cnt");
		return mapList(playerService.getPlayerRoundCnt(), PlayerRoundCntDto.class);
	}

	@Tag(name = "Round API")
	@Operation(summary = "Swap player in the round")
	@PatchMapping(value = "/rest/SwapPlrRnd")
	public HttpStatus swapPlayerRnd( @Valid
									 @Parameter(description = "Swap Player Round DTO object", required = true) @RequestBody SwapPlrRndDto playerUpdateDto) {

		log.info("trying to swap player id: " + playerUpdateDto.getOldPlrId() + " with new player id " +
				playerUpdateDto.getNewPlrId() + " for round id: " + playerUpdateDto.getRoundId());

		roundService.swapPlayer(playerUpdateDto.getOldPlrId(), playerUpdateDto.getNewPlrId(), playerUpdateDto.getRoundId());

		return HttpStatus.OK;
	}
}
