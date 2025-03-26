package com.greg.golf.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.greg.golf.captcha.ICaptchaService;
import com.greg.golf.configurationproperties.PlayerServiceConfig;
import com.greg.golf.error.GeneralException;
import com.greg.golf.error.TooShortStringForSearchException;
import com.greg.golf.repository.projection.PlayerRoundCnt;
import com.greg.golf.security.JwtTokenUtil;
import com.greg.golf.security.RefreshTokenUtil;
import com.greg.golf.security.aes.StringUtility;
import com.greg.golf.service.helpers.RoleVerification;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Slf4j
@Service("playerService")
@CacheConfig(cacheNames = { "player" })
public class PlayerService {

	private final PlayerRepository playerRepository;
	private final PlayerServiceConfig playerServiceConfig;
	private final JwtTokenUtil jwtTokenUtil;
	private final RefreshTokenUtil refreshTokenUtil;
	private final ICaptchaService captchaService;
	private final PasswordEncoder bCryptPasswordEncoder;
	private final AuthenticationManager authenticationManager;

	@Lazy
	private final PlayerService self;

	@Transactional
	public GolfUserDetails authenticatePlayer(Player player) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(player.getNick(), player.getPassword()));
		log.debug("Authentication completed");
		return loadUserAndUpdate(player.getNick());
	}

	@Transactional
	public String processOAuthPostLogin(String firstName, String lastName, int playerType) {

		String nick = firstName + "." + lastName.substring(0,2);
		StringBuilder queryParams = new StringBuilder("?token=");
		String newPlayerQuery = "";

		// check if player already exists
		Player player = self.getPlayerForNick(nick);

		if (player == null) {
            log.info("Social media player not found: {} - adding the new player for {} {}", nick, firstName, lastName);

			final var newPlayer = new Player();
			newPlayer.setNick(nick);
			// set default values for whs and sex - they should be updated in the second step on frontend
			newPlayer.setWhs(54.0F);
			newPlayer.setSex(false);
			newPlayer.setPassword((playerServiceConfig.getTempPwd()));
			newPlayer.setType(playerType);
			self.addPlayerOnBehalf(newPlayer);
			newPlayerQuery = "&new_player=true";
		} else {
            log.debug("Player with such nick already exists: {}", nick);
			if (player.getType() != playerType) {
				log.error("Attempt to log a player with different social media than registered");
                log.error("Expected: {} attempt with: {}", player.getType(), playerType);
				return null;
			}
		}

		queryParams.append(generateJwtToken(loadUserAndUpdate(nick)));
		queryParams.append(newPlayerQuery);

		return queryParams.toString();
	}

	public String generateJwtToken(GolfUserDetails userDetails) {
		return jwtTokenUtil.generateToken(userDetails);
	}

	public String generateRefreshToken(GolfUserDetails userDetails) {

		var refresh =refreshTokenUtil.generateToken(userDetails);
		var player = userDetails.getPlayer();
		player.setRefresh(refresh);
		playerRepository.save(player);
		return refresh;
	}

	@Transactional
	public void addPlayer(Player player) {
		captchaService.processResponse(player.getCaptcha());

		player.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
		player.setType(Common.TYPE_PLAYER_LOCAL);

		save(player);
	}

	@Transactional
	public Player addPlayerOnBehalf(Player player) {

		player.setPassword(bCryptPasswordEncoder.encode( playerServiceConfig.getTempPwd()));

		// social player type should be set earlier
		if (player.getType() == null) {
			player.setType(Common.TYPE_PLAYER_LOCAL);
		}

		return save(player);
	}

	@CacheEvict
	@Transactional
	public void delete(Long id) {

		var player = new Player();
		player.setId(id);

		playerRepository.delete(player);
	}

	private Player save(Player player) {

		try {
			player.setRole(Common.ROLE_PLAYER_REGULAR);
			player.setModified(false);
			return playerRepository.save(player);
		} catch (Exception e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				throw new PlayerNickInUseException();
			}
			throw e;
		}

	}

	private GolfUserDetails loadUserAndUpdate(String playerName) {

		Player player = playerRepository.findPlayerByNick(playerName)
				.orElseThrow(() -> new UsernameNotFoundException("User " + playerName + " not found"));

		// clear modify flag if user has been changed
		if (Boolean.TRUE.equals(player.getModified())) {

			player.setModified(false);
			playerRepository.save(player);
            log.debug("Cleared modified flag for user {}", playerName);
		}

		log.info("Creating user details for player name {}",  playerName);

		return new GolfUser(player.getNick(), player.getPassword(), new ArrayList<>(), player);
	}

	@SuppressWarnings("unused")
	@CacheEvict(value = "player", key = "#player.id")
	public void cacheEvict(@NonNull Player player) {
		log.debug("Cache evict called");
	}

	@Transactional(readOnly = true)
	public GolfUserDetails loadUserById(Long id) {

		Player player = playerRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User " + id + " not found"));

        log.info("Creating user details for id  {}", id);

		return new GolfUser(player.getNick(), player.getPassword(),	new ArrayList<>(), player);
	}

	@Cacheable(value = "player", key = "#id")
	@Transactional(readOnly = true)
	public Optional<Player> getPlayer(Long id) {
		log.debug("Get player called");
		return playerRepository.findById(id);
	}

	@CacheEvict(value = "player", key = "#player.id")
	@Transactional
	public Player update(@NonNull Player player) {

		// player can be updated only by himself
		RoleVerification.verifyPlayer(player.getId(), "Attempt to update player result by unauthorized user");

		var persistedPlayer = playerRepository.findById(player.getId()).orElseThrow();

		if (player.getWhs() != null) {
			persistedPlayer.setWhs(player.getWhs());
			log.info("Handicap changed by player");
		}
		if (player.getPassword() != null && !player.getPassword().isEmpty()) {
			persistedPlayer.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
			log.info("Password changed by player");
		}

		if (player.getEmail() != null && !player.getEmail().isEmpty()) {
			persistedPlayer.setEmail(encryptEmail(player.getEmail()));
			log.info("Email changed by player");
		}

		playerRepository.save(persistedPlayer);

		return persistedPlayer;
	}

	@CacheEvict(value = "player", key = "#player.id")
	@Transactional
	public void updatePlayerOnBehalf(@NonNull Player player, boolean updateSocial) {

		var persistedPlayer = playerRepository.findById(player.getId()).orElseThrow();
		boolean changed = false;

		if (player.getWhs() != null && !player.getWhs().equals(persistedPlayer.getWhs()) ) {
			persistedPlayer.setWhs(player.getWhs());
			changed = true;
		}

		if (player.getNick() != null && !player.getNick().equals(persistedPlayer.getNick()) ) {
			persistedPlayer.setNick(player.getNick());
			changed = true;
		}

		if (player.getSex() != null && !player.getSex().equals(persistedPlayer.getSex()) ) {
			persistedPlayer.setSex(player.getSex());
			changed = true;
		}

		if (changed && !updateSocial) {
			persistedPlayer.setModified(true);
			playerRepository.save(persistedPlayer);
            log.debug("player changes saved for {}", persistedPlayer.getNick());
		} else {
            log.warn("nothing to update for player {}", persistedPlayer.getNick());
		}
	}

	@Transactional
	public void resetPassword(Player player) {

		if (player.getPassword() != null && !player.getPassword().isEmpty()) {
			Player persistedPlayer = playerRepository.findPlayerByNick(player.getNick()).orElseThrow();
			persistedPlayer.setPassword(bCryptPasswordEncoder.encode(player.getPassword()));
			playerRepository.save(persistedPlayer);
		}

	}

	@Transactional(readOnly = true)
	public Player getPlayerForNick(String nick) {

		Optional<Player> player = playerRepository.findPlayerByNick(nick);

		return player.orElse(null);

	}

	@Transactional
	public List<PlayerRoundCnt> getPlayerRoundCnt() {
		return playerRepository.getPlayerRoundCnt();
	}

	@Transactional(readOnly = true)
	public List<Player> searchForPlayer(String nick, Integer pageNo) throws TooShortStringForSearchException {

		if (nick.length() < playerServiceConfig.getMinSearchLength()) {
			throw new TooShortStringForSearchException();
		}

		return playerRepository.findByNickContainingIgnoreCaseOrderByNickAsc(nick,
				PageRequest.of(pageNo, playerServiceConfig.getPageSize()));
	}

	private String encryptEmail(String email) throws GeneralException {

		String encryptedEmail = null;

		if (email != null) {
			try {
				encryptedEmail = StringUtility.encryptString(email, playerServiceConfig.getEmailPwd());
			} catch(Exception e) {
				throw new GeneralException();
			}
		}

		return encryptedEmail;
	}

	@Transactional(readOnly = true)
	public String getEmail(Long id) throws GeneralException {

		String decryptedEmail = null;

		Player player = playerRepository.findById(id).orElseThrow();

		if (player.getEmail() != null) {
			try {
				decryptedEmail = StringUtility.decryptString(player.getEmail(), playerServiceConfig.getEmailPwd());
			} catch(Exception e) {
				throw new GeneralException();
			}
		}

		return decryptedEmail;
	}

	@Transactional
	public void deleteEmail() {

		Long playerId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("Trying to remove email for player: {}", playerId);

		var player = playerRepository.findById(playerId).orElseThrow();
		player.setEmail(null);
		playerRepository.save(player);

	}
}
