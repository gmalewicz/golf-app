package com.greg.golf.service;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.PlayerNickInUseException;
import com.greg.golf.error.UnauthorizedException;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service("playerService")
@CacheConfig(cacheNames = { "player" })
public class PlayerService implements UserDetailsService {

	private final PlayerRepository playerRepository;

	@Transactional
	public Player save(Player player) {

		try {
			player.setRole(Common.ROLE_PLAYER_REGULAR);
			return playerRepository.save(player);
		} catch (Exception e) {
			if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
				throw new PlayerNickInUseException();
			}
			throw e;
		}

	}

	@Override
	@Transactional(readOnly = true)
	public GolfUserDetails loadUserByUsername(String playerName) throws UsernameNotFoundException {

		Optional<Player> player = playerRepository.findPlayerByNick(playerName);
		GolfUserDetails golfUserDetails;

		try {
			golfUserDetails = new GolfUser(player.orElseThrow().getNick(), player.orElseThrow().getPassword(),
					new ArrayList<>(), player.orElseThrow());
			log.info("User details created");
		} catch (NoSuchElementException e) {
			throw new UsernameNotFoundException("User not found", e.getCause());
		}

		return golfUserDetails;

	}

	@Transactional(readOnly = true)
	public GolfUserDetails loadUserById(Long id) {

		Optional<Player> player = playerRepository.findById(id);
		GolfUserDetails golfUserDetails;

		golfUserDetails = new GolfUser(player.orElseThrow().getNick(), player.orElseThrow().getPassword(),
				new ArrayList<>(), player.orElseThrow());
		log.info("User details created");

		return golfUserDetails;

	}

	@Cacheable
	@Transactional(readOnly = true)
	public Optional<Player> getPlayer(Long id) {
		log.info("Get player called");
		return playerRepository.findById(id);
	}

	@CacheEvict(key = "#player.id")
	@Transactional
	public Player update(Player player) {

		var persistedPlayer = playerRepository.getById(player.getId());

		if (player.getWhs() != null) {
			persistedPlayer.setWhs(player.getWhs());
		}
		if (player.getPassword() != null && !player.getPassword().equals("")) {
			persistedPlayer.setPassword(player.getPassword());
		}

		persistedPlayer.setRole(persistedPlayer.getRole());
		playerRepository.save(persistedPlayer);

		return persistedPlayer;
	}

	@Transactional
	public Player resetPassword(Player player) {
		
		Player persistedPlayer = null;
		
		if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority()
				.equals(Common.ADMIN)) {
			
			persistedPlayer = playerRepository.findPlayerByNick(player.getNick()).orElseThrow();

			if (player.getPassword() != null && !player.getPassword().equals("")) {
				persistedPlayer.setPassword(player.getPassword());
			}

			persistedPlayer = playerRepository.save(persistedPlayer);
						
		} else {
			log.error("Attempt to reset password by unauthorized user");
			throw new UnauthorizedException();
		}
		
		return persistedPlayer;
	}

	@Transactional(readOnly = true)
	public Player getPlayerForNick(String nick) {

		Optional<Player> player = playerRepository.findPlayerByNick(nick);

		if (player.isEmpty()) {
			return null;
		}

		return player.get();
	}
}
