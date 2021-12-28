package com.greg.golf.service;

import com.greg.golf.entity.Player;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.service.helpers.GolfUser;
import com.greg.golf.service.helpers.GolfUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@RequiredArgsConstructor
@Slf4j
@Service("userService")
public class UserService implements UserDetailsService {

	private final PlayerRepository playerRepository;

	@Override
	@Transactional(readOnly = true)
	public GolfUserDetails loadUserByUsername(String playerName) throws UsernameNotFoundException {

		Player player = playerRepository.findPlayerByNick(playerName)
				.orElseThrow(() -> new UsernameNotFoundException("User " + playerName + " not found"));

		log.info("Creating user details for " + playerName);

		return new GolfUser(player.getNick(), player.getPassword(),	new ArrayList<>(), player);

	}
}
