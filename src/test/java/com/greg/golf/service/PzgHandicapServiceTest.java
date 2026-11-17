package com.greg.golf.service;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.web.server.ResponseStatusException;

import com.greg.golf.configurationproperties.PzgConfig;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.repository.projection.PlayerPzgId;
import com.sun.net.httpserver.HttpServer;

class PzgHandicapServiceTest {

	private HttpServer server;
	private PzgConfig config;
	private PlayerRepository playerRepository;
	private CacheManager cacheManager;
	private final AtomicInteger loginCnt = new AtomicInteger();
	private final AtomicInteger profileCnt = new AtomicInteger();
	private volatile String profileBody;
	private volatile String lastLoginBody;
	private volatile boolean rejectFirstProfile;
	private volatile boolean failProfile;
	private volatile boolean failFirstLogin;

	@BeforeEach
	void setup() throws IOException {

		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/api/_account1/auth/login", ex -> {
			loginCnt.incrementAndGet();
			try (var in = ex.getRequestBody()) {
				lastLoginBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			}
			reply(ex, loginCnt.get() == 1 && failFirstLogin ? 500 : 200, "{\"token\":\"t" + loginCnt.get() + "\"}");
		});
		server.createContext("/api/_account1/member/get_profile", ex -> {
			var call = profileCnt.incrementAndGet();
			if (failProfile) {
				reply(ex, 500, "{}");
			} else if (rejectFirstProfile && call == 1) {
				reply(ex, 401, "{}");
			} else {
				reply(ex, 200, profileBody);
			}
		});
		server.start();

		config = new PzgConfig();
		config.setEnabled(true);
		config.setUrl("http://localhost:" + server.getAddress().getPort());
		config.setUsername("user");
		config.setPassword("pwd");
		config.setTimeoutMs(1000);

		playerRepository = mock(PlayerRepository.class);
		cacheManager = new ConcurrentMapCacheManager("player");
	}

	@AfterEach
	void tearDown() {
		server.stop(0);
	}

	private static void reply(com.sun.net.httpserver.HttpExchange ex, int status, String body) throws IOException {
		var bytes = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json");
		ex.sendResponseHeaders(status, bytes.length);
		try (var os = ex.getResponseBody()) {
			os.write(bytes);
		}
	}

	@Test
	void disabledNeverCallsPzgTest() {

		config.setEnabled(false);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateHandicap(1L, 100L);

		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
		org.junit.jupiter.api.Assertions.assertEquals(0, loginCnt.get());
		org.junit.jupiter.api.Assertions.assertEquals(0, profileCnt.get());
	}

	@Test
	void updatesWhsAndIgnoresOtherDataTest() {

		profileBody = "{\"member\":{\"hcp\":12.3,\"photo\":\"abc\",\"name\":\"X\"},\"other\":1}";
		when(playerRepository.updateWhs(1L, 12.3F)).thenReturn(1);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateHandicap(1L, 100L);

		verify(playerRepository).updateWhs(1L, 12.3F);
		org.junit.jupiter.api.Assertions.assertTrue(lastLoginBody.contains("\"email\":\"user\"")
				&& lastLoginBody.contains("\"password\":\"pwd\""));
	}

	@Test
	void reusesTokenAndRelogsInOn401Test() {

		profileBody = "{\"member\":{\"hcp\":\"7,5\"}}";
		rejectFirstProfile = true;
		when(playerRepository.updateWhs(1L, 7.5F)).thenReturn(1);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateHandicap(1L, 100L);
		org.junit.jupiter.api.Assertions.assertEquals(2, loginCnt.get());

		service.updateHandicap(1L, 100L);
		org.junit.jupiter.api.Assertions.assertEquals(2, loginCnt.get());
		verify(playerRepository, times(2)).updateWhs(1L, 7.5F);
	}

	@Test
	void invalidOrMissingHcpKeepsWhsTest() {

		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		profileBody = "{\"member\":{\"hcp\":\"abc\"}}";
		service.updateHandicap(1L, 100L);
		profileBody = "{\"member\":{\"hcp\":99}}";
		service.updateHandicap(1L, 100L);
		profileBody = "{}";
		service.updateHandicap(1L, 100L);

		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
	}

	@Test
	void serviceUnavailableDoesNotThrowTest() {

		server.stop(0);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateHandicap(1L, 100L);

		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
	}

	private static PlayerPzgId pzgPlayer(long id, long pzgId) {
		var p = mock(PlayerPzgId.class);
		when(p.getId()).thenReturn(id);
		when(p.getPzgId()).thenReturn(pzgId);
		return p;
	}

	@Test
	void scheduledJobUpdatesAllPlayersWithPzgIdTest() {

		profileBody = "{\"member\":{\"hcp\":\"16.5\"}}";
		var players = List.of(pzgPlayer(1, 100), pzgPlayer(2, 200));
		when(playerRepository.findPlayersWithPzgId()).thenReturn(players);
		when(playerRepository.updateWhs(anyLong(), anyFloat())).thenReturn(1);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateAllHandicaps();

		verify(playerRepository).updateWhs(1L, 16.5F);
		verify(playerRepository).updateWhs(2L, 16.5F);
		org.junit.jupiter.api.Assertions.assertEquals(1, loginCnt.get());
		org.junit.jupiter.api.Assertions.assertEquals(2, profileCnt.get());
	}

	@Test
	void scheduledJobDisabledDoesNothingTest() {

		config.setEnabled(false);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateAllHandicaps();

		verify(playerRepository, never()).findPlayersWithPzgId();
		org.junit.jupiter.api.Assertions.assertEquals(0, loginCnt.get());
	}

	@Test
	void scheduledJobAbortsWhenPzgKeepsFailingTest() {

		failProfile = true;
		var players = List.of(pzgPlayer(1, 1), pzgPlayer(2, 2), pzgPlayer(3, 3), pzgPlayer(4, 4),
				pzgPlayer(5, 5), pzgPlayer(6, 6), pzgPlayer(7, 7));
		when(playerRepository.findPlayersWithPzgId()).thenReturn(players);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		service.updateAllHandicaps();

		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
		org.junit.jupiter.api.Assertions.assertEquals(10, profileCnt.get());
	}

	@Test
	void retriesOnceWhenFirstLoginFailsTest() {

		failFirstLogin = true;
		profileBody = "{\"member\":{\"hcp\":\"9.1\"}}";
		when(playerRepository.updateWhs(1L, 9.1F)).thenReturn(1);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertTrue(service.updateHandicap(1L, 100L));

		org.junit.jupiter.api.Assertions.assertEquals(2, loginCnt.get());
		verify(playerRepository).updateWhs(1L, 9.1F);
	}

	private int onDemandStatus(PzgHandicapService service, long playerId) {
		return org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
				() -> service.updateHandicapOnDemand(playerId)).getStatusCode().value();
	}

	private Player playerWithPzgId(Long pzgId) {
		var player = new Player();
		player.setPzgId(pzgId);
		when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
		return player;
	}

	@Test
	void onDemandUpdatesWhsTest() {

		profileBody = "{\"member\":{\"hcp\":\"8.4\"}}";
		playerWithPzgId(100L);
		when(playerRepository.updateWhs(1L, 8.4F)).thenReturn(1);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(8.4F, service.updateHandicapOnDemand(1L));

		verify(playerRepository).updateWhs(1L, 8.4F);
	}

	@Test
	void onDemandDisabledReturnsConflictTest() {

		config.setEnabled(false);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(409, onDemandStatus(service, 1L));
		org.junit.jupiter.api.Assertions.assertEquals(0, loginCnt.get());
		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
	}

	@Test
	void onDemandUnknownPlayerReturnsNotFoundTest() {

		when(playerRepository.findById(1L)).thenReturn(Optional.empty());
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(404, onDemandStatus(service, 1L));
	}

	@Test
	void onDemandPlayerWithoutPzgIdReturnsBadRequestTest() {

		playerWithPzgId(null);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(400, onDemandStatus(service, 1L));
		org.junit.jupiter.api.Assertions.assertEquals(0, loginCnt.get());
	}

	@Test
	void onDemandInvalidHcpReturnsBadGatewayTest() {

		profileBody = "{\"member\":{\"hcp\":99}}";
		playerWithPzgId(100L);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(502, onDemandStatus(service, 1L));
		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
	}

	@Test
	void onDemandServiceUnavailableReturnsBadGatewayTest() {

		playerWithPzgId(100L);
		server.stop(0);
		var service = new PzgHandicapService(config, playerRepository, cacheManager);

		org.junit.jupiter.api.Assertions.assertEquals(502, onDemandStatus(service, 1L));
		verify(playerRepository, never()).updateWhs(anyLong(), anyFloat());
	}
}
