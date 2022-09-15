package com.greg.golf.controller;

import javax.servlet.http.HttpServletRequest;
import com.greg.golf.controller.dto.PlayerIdDto;
import com.greg.golf.security.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.greg.golf.controller.dto.PlayerDto;
import com.greg.golf.entity.Player;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.helpers.GolfUserDetails;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@OpenAPIDefinition(tags = @Tag(name = "Access API"))
public class AccessController {

	private static final String REFRESH_TOKEN = "refreshToken";

	private final PlayerService playerService;
	private final ModelMapper modelMapper;
	private final JwtTokenUtil jwtTokenUtil;

	@Tag(name = "Access API")
	@Operation(summary = "Authenticate player with given nick name and password. WHS is not relevant.")
	@PostMapping(value = "/rest/Authenticate")
	public ResponseEntity<PlayerDto> authenticatePlayer(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to authenticate player: " + playerDto.getNick() + " with password *****");

		GolfUserDetails userDetails =playerService.authenticatePlayer(modelMapper.map(playerDto, Player.class));

		var responseHeaders = new HttpHeaders();
		responseHeaders.set("Access-Control-Expose-Headers", "Jwt");
		responseHeaders.set("Jwt", playerService.generateJwtToken(userDetails));
		responseHeaders.set("Refresh", playerService.generateRefreshToken(userDetails));

		return new ResponseEntity<>(modelMapper.map(userDetails.getPlayer(), PlayerDto.class), responseHeaders,
				HttpStatus.OK);
	}

	@Tag(name = "Access API")
	@Operation(summary = "Get Social media player data.")
	@GetMapping(value = "/rest/GetSocialPlayer")
	public ResponseEntity<PlayerDto> getSocialPlayer(HttpServletRequest request) {

		String requestTokenHeader = request.getHeader("Authorization");

		String userId = jwtTokenUtil.getUserIdFromToken(requestTokenHeader.substring(7));
		GolfUserDetails userDetails = playerService.loadUserById(Long.valueOf(userId));
		log.info("get data for social player : " + userDetails.getPlayer().getNick());

		var responseHeaders = new HttpHeaders();
		responseHeaders.set("refresh", playerService.generateRefreshToken(userDetails));

		return new ResponseEntity<>(modelMapper.map(userDetails.getPlayer(), PlayerDto.class), responseHeaders, HttpStatus.OK);
	}


	@Tag(name = "Access API")
	@Operation(summary = "Add player.")
	@PostMapping(value = "/rest/AddPlayer")
	public HttpStatus addPlayer(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to add player: " + playerDto.getNick());

		playerService.addPlayer(modelMapper.map(playerDto, Player.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Update player. Only WHS and/or password can be updated.")
	@PatchMapping(value = "/rest/PatchPlayer")
	public ResponseEntity<PlayerDto> updatePlayer(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to update player: " + playerDto.getNick());

		Player player = playerService.update(modelMapper.map(playerDto, Player.class));

		return new ResponseEntity<>(modelMapper.map(player, PlayerDto.class), HttpStatus.OK);
	}

	@SuppressWarnings({"UnusedReturnValue"})
	@Tag(name = "Access API")
	@Operation(summary = "Administrative task: Reset password.")
	@PatchMapping(value = "/rest/ResetPassword")
	public HttpStatus resetPassword(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to reset the password for player: " + playerDto.getNick());

		playerService.resetPassword(modelMapper.map(playerDto, Player.class));

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Add player on behalf of him. It will have temporary 'welcome' password.")
	@PostMapping(value = "/rest/AddPlayerOnBehalf")
	public ResponseEntity<PlayerDto> addPlayerOnBehalf(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to add player on behalf: " + playerDto.getNick() + " with temporary password");

		Player player = playerService.addPlayerOnBehalf(modelMapper.map(playerDto, Player.class));

		return new ResponseEntity<>(modelMapper.map(player, PlayerDto.class), HttpStatus.OK);
	}

	@SuppressWarnings("UnusedReturnValue")
	@Tag(name = "Access API")
	@Operation(summary = "Refresh player token.")
	@GetMapping(value = "/rest/Refresh/{id}")
	public ResponseEntity<String> refreshToken(HttpServletRequest request,
			@Parameter(required = true, description = "Id of the player") @PathVariable("id") Long id) {

		log.debug("trying to refresh token for player id: " + id);

		var responseHeaders = new HttpHeaders();

		if (request.getAttribute(REFRESH_TOKEN) != null) {

			updateRefreshHeader(responseHeaders, id);
		}

		return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
	}

	@SuppressWarnings("UnusedReturnValue")
	@Tag(name = "Access API")
	@Operation(summary = "Refresh player JWT on demand")
	@GetMapping(value = "/rest/RefreshToken/{id}")
	public ResponseEntity<String> refreshTokenOnDemand(HttpServletRequest request,
											   @Parameter(required = true, description = "Id of the player") @PathVariable("id") Long id) {

		log.debug("trying to refresh token on demand for player id: " + id);

		var responseHeaders = new HttpHeaders();

		updateRefreshHeader(responseHeaders, id);

		return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
	}

	private void updateRefreshHeader(HttpHeaders responseHeaders, Long id) {

		final GolfUserDetails userDetails = playerService.loadUserById(id);

		responseHeaders.set("Access-Control-Expose-Headers", "Jwt");
		responseHeaders.set("Jwt",  playerService.generateJwtToken(userDetails));
		// regenerate refresh token
		responseHeaders.set("Refresh", playerService.generateRefreshToken(userDetails));

	}


	@Tag(name = "Access API")
	@Operation(summary = "Delete player")
	@PostMapping(value = "/rest/DeletePlayer")
	public HttpStatus deletePlayer(
			@Parameter(description = "Player id", required = true) @RequestBody PlayerIdDto playerIdDto) {

		log.info("trying to delete player with id: " + playerIdDto.getId());

		playerService.delete(playerIdDto.getId());

		return HttpStatus.OK;
	}

	@Tag(name = "Access API")
	@Operation(summary = "Update player by administrator.")
	@PatchMapping(value = "/rest/UpdatePlayerOnBehalf")
	public HttpStatus updatePlayerOnBehalf(
			@Parameter(description = "Player DTO object", required = true) @RequestBody PlayerDto playerDto) {

		log.info("trying to update player: " + playerDto.getNick() + " by admin or other player");
		boolean updateSocial = playerDto.getUpdateSocial() != null && playerDto.getUpdateSocial();

		playerService.updatePlayerOnBehalf(modelMapper.map(playerDto, Player.class), updateSocial);

		return HttpStatus.OK;
	}
}
