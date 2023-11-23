package com.greg.golf.service;

import com.greg.golf.entity.Player;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AddPlayerTest {

    @ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer.getInstance();

    @Autowired
    private PlayerService playerService;

    @DisplayName("Add player test")
    @Transactional
    @Test
    void addPlayerTest() {

        Player player = new Player();
        player.setNick("test");
        player.setPassword("welcome");
        player.setCaptcha("ABCDE");
        player.setWhs(10.0F);
        player.setSex(Common.PLAYER_SEX_MALE);

        Assertions.assertDoesNotThrow(() -> playerService.addPlayer(player));

    }
}
