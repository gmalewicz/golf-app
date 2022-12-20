package com.greg.golf.controller;

import java.util.List;
import java.util.NoSuchElementException;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.controller.dto.GameDto;
import com.greg.golf.entity.Game;
import com.greg.golf.entity.Player;
import com.greg.golf.error.SendingMailFailureException;

import com.greg.golf.service.GameService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@OpenAPIDefinition(tags = @Tag(name = "Game API"))
public class GameController extends BaseController {

	private final GameService gameService;

	public GameController(GameService gameService, ModelMapper modelMapper) {
		super(modelMapper);
		this.gameService = gameService;
	}

	@SuppressWarnings("SameReturnValue")
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

		var player = new Player();
		player.setId(id);

		return mapList(gameService.listByPlayer(player), GameDto.class);
	}

	@SuppressWarnings("SameReturnValue")
	@Tag(name = "Game API")
	@Operation(summary = "Sends game details for email address")
	@PostMapping(value = "/rest/SendGame")
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
}
