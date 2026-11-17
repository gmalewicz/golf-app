package com.greg.golf.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.greg.golf.configurationproperties.PzgConfig;
import com.greg.golf.repository.PlayerRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PzgHandicapService {

	private static final String LOGIN_PATH = "/api/_account1/auth/login";
	private static final String PROFILE_PATH = "/api/_account1/member/get_profile?id={id}";
	private static final float MIN_WHS = -5.0F;
	private static final float MAX_WHS = 54.0F;
	private static final int MAX_CONSECUTIVE_FAILURES = 5;

	private final PzgConfig pzgConfig;
	private final PlayerRepository playerRepository;
	private final CacheManager cacheManager;
	private final RestClient restClient;
	private final AtomicReference<String> token = new AtomicReference<>();

	@JsonIgnoreProperties(ignoreUnknown = true)
	record LoginResponse(String token) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Member(Object hcp) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record ProfileResponse(Member member) {
	}

	public PzgHandicapService(PzgConfig pzgConfig, PlayerRepository playerRepository, CacheManager cacheManager) {

		this.pzgConfig = pzgConfig;
		this.playerRepository = playerRepository;
		this.cacheManager = cacheManager;

		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(pzgConfig.getTimeoutMs());
		requestFactory.setReadTimeout(pzgConfig.getTimeoutMs());
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	public boolean isEnabled() {
		return pzgConfig.isEnabled();
	}

	/**
	 * Nightly refresh of the handicap of every player that has pzgId. Does nothing when pzg.enabled is false.
	 * A run is aborted when PZG keeps failing, so an outage does not make it last for hours.
	 */
	@Scheduled(cron = "0 0 3 * * *", zone = "Europe/Warsaw")
	public void updateAllHandicaps() {

		if (!pzgConfig.isEnabled()) {
			return;
		}

		var players = playerRepository.findPlayersWithPzgId();
		log.info("PZG handicap update started for {} players", players.size());

		int updated = 0;
		int consecutiveFailures = 0;

		for (var player : players) {
			if (updateHandicap(player.getId(), player.getPzgId())) {
				updated++;
				consecutiveFailures = 0;
			} else if (++consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
				log.error("PZG handicap update aborted after {} consecutive failures", consecutiveFailures);
				break;
			}
		}

		log.info("PZG handicap update finished: {} of {} players updated", updated, players.size());
	}

	/**
	 * Reads the handicap of the player from PZG and stores it in Player.whs. Never throws - any problem is logged
	 * and the existing whs value is kept.
	 *
	 * @return true when a valid handicap was stored
	 */
	public boolean updateHandicap(Long playerId, Long pzgId) {

		if (!pzgConfig.isEnabled() || playerId == null || pzgId == null) {
			return false;
		}

		try {
			var whs = fetchHandicap(pzgId);
			whs.ifPresent(value -> store(playerId, value));
			return whs.isPresent();
		} catch (Exception e) {
			log.warn("PZG handicap update failed for player id {} (pzgId {}): {}", playerId, pzgId, e.getMessage());
			return false;
		}
	}

	private Optional<Float> fetchHandicap(Long pzgId) {

		ProfileResponse profile;
		try {
			profile = getProfile(pzgId, currentToken());
		} catch (HttpClientErrorException.Unauthorized e) {
			log.debug("PZG token expired - logging in again");
			token.set(null);
			profile = getProfile(pzgId, currentToken());
		}

		if (profile == null || profile.member() == null || profile.member().hcp() == null) {
			log.warn("PZG returned no handicap for pzgId {}", pzgId);
			return Optional.empty();
		}

		try {
			var whs = Float.parseFloat(profile.member().hcp().toString().trim().replace(',', '.'));
			if (Float.isNaN(whs) || whs < MIN_WHS || whs > MAX_WHS) {
				log.warn("PZG returned handicap out of range for pzgId {}: {}", pzgId, whs);
				return Optional.empty();
			}
			return Optional.of(whs);
		} catch (NumberFormatException e) {
			log.warn("PZG returned invalid handicap for pzgId {}: {}", pzgId, profile.member().hcp());
			return Optional.empty();
		}
	}

	private ProfileResponse getProfile(Long pzgId, String bearer) {

		return restClient.get().uri(pzgConfig.getUrl() + PROFILE_PATH, pzgId)
				.headers(h -> h.setBearerAuth(bearer)).retrieve().body(ProfileResponse.class);
	}

	private String currentToken() {

		var current = token.get();
		if (current != null) {
			return current;
		}

		var response = restClient.post().uri(pzgConfig.getUrl() + LOGIN_PATH)
				.body(Map.of("email", pzgConfig.getUsername(), "password", pzgConfig.getPassword()))
				.retrieve().body(LoginResponse.class);

		if (response == null || response.token() == null || response.token().isBlank()) {
			throw new IllegalStateException("PZG login did not return a token");
		}

		token.set(response.token());
		log.debug("PZG login completed");
		return response.token();
	}

	private void store(Long playerId, float whs) {

		if (playerRepository.updateWhs(playerId, whs) > 0) {
			Cache cache = cacheManager.getCache("player");
			if (cache != null) {
				cache.evict(playerId);
			}
			log.info("Handicap of player id {} updated from PZG to {}", playerId, whs);
		}
	}
}
