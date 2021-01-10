package com.greg.golf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.greg.golf.captcha.ICaptchaService;
import com.greg.golf.entity.Player;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.service.PlayerService;
import com.greg.golf.service.helpers.GolfUserDetails;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@OpenAPIDefinition(tags = { @Tag(name = "Access API") })
public class AccessController {

	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private PlayerService playerService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
    private ICaptchaService captchaService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Tag(name = "Access API")
	@Operation(summary = "Authenticate player with given nick name and password. WHS is not relevant.")
	@PostMapping(value = "/rest/Authenticate")
	public ResponseEntity<Player> authenticatePlayer(
			@Parameter(description = "Player object", required = true) @RequestBody Player player) throws Exception {

		log.debug("trying to authenticate player: " + player.getNick() + " with password " + player.getPassword());

		authenticate(player.getNick(), player.getPassword());

		final GolfUserDetails userDetails = playerService.loadUserByUsername(player.getNick());

		final String token = jwtTokenUtil.generateToken(userDetails);

		log.info(userDetails.getPlayer());

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Access-Control-Expose-Headers", "Jwt");
		responseHeaders.set("Jwt", token);

		// userDetails.getPlayer().setToken(token);
		// userDetails.getPlayer().setPassword(null);

		return new ResponseEntity<Player>(userDetails.getPlayer(), responseHeaders, HttpStatus.OK);
	}

	@Tag(name = "Access API")
	@Operation(summary = "Add player.")
	@PostMapping(value = "/rest/AddPlayer")
	public HttpStatus addPlayer(@Parameter(description = "Player object", required = true) @RequestBody Player player) {

		log.info("trying to add player: " + player.getNick() + " with password " + player.getPassword());
		
		captchaService.processResponse(player.getCaptcha());

		player.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));

		playerService.save(player);

		return HttpStatus.OK;
	}
	
	private void authenticate(String username, String password) throws BadCredentialsException {
		//try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		//} catch (DisabledException e) {
		//	throw new Exception("USER_DISABLED", e);
		//} catch (BadCredentialsException e) {
		//	throw new Exception("INVALID_CREDENTIALS", e);
		//}
	}
	
	@Tag(name = "Access API")
	@Operation(summary = "Update player. Only WHS and/or password can be updated.")
	@PatchMapping(value = "/rest/PatchPlayer/{id}")
	public ResponseEntity<Player> updatePlayer(
			@Parameter(required = true, description = "Id of the player for update") @PathVariable("id") Long id,
			@Parameter(description = "Player object", required = true) @RequestBody Player player) {

		log.info("trying to update player: " + player.getNick());

		if (player.getPassword() != null && !player.getPassword().equals("")) {

			log.info("password changed");
			player.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
		}

		player = playerService.update(player);

		return new ResponseEntity<Player>(player, HttpStatus.OK);
	}

	
	@Tag(name = "Access API")
	@Operation(summary = "Administrative task: Reset password.")
	@PatchMapping(value = "/rest/ResetPassword/{id}")
	public HttpStatus resetPassword(
			@Parameter(required = true, description = "Id of the player who performs the update") @PathVariable("id") Long id,
			@Parameter(description = "Player object", required = true) @RequestBody Player player) {

		log.info("trying to reset the password for player: " + player.getNick());

		if (player.getPassword() != null && !player.getPassword().equals("")) {

			log.info("password changed");
			player.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
		}

		player = playerService.resetPassword(id, player);

		return HttpStatus.OK;
	}
}
