package com.greg.golf.service;

import com.greg.golf.controller.dto.EagleResultDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleResult;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.entity.Player;
import com.greg.golf.repository.CycleRepository;
import com.greg.golf.repository.CycleResultRepository;
import com.greg.golf.repository.CycleTournamentRepository;
import com.greg.golf.security.JwtRequestFilter;
import com.greg.golf.util.GolfPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
class CycleServiceTest {

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @ClassRule
    public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
            .getInstance();

    private Cycle cycle;
    private EagleResultDto eagleResultDto;
    private static Player player;

    @SuppressWarnings("unused")
    @Autowired
    private CycleService cycleService;

    @BeforeAll
    static void setup(@Autowired PlayerService playerService) {

        player = playerService.getPlayer(1L).orElseThrow();

        log.info("Set up completed");
    }

    @BeforeEach
    void setupEachTest() {

        cycle = new Cycle();
        cycle.setName("Test cycle");
        cycle.setStatus(Cycle.STATUS_OPEN);
        cycle.setPlayer(player);
        cycle.setBestRounds(1);
        cycle.setVersion(1);
        cycle.setMaxWhs(12.0F);
        cycle.setSeries(1);

        eagleResultDto = new EagleResultDto();
        eagleResultDto.setR(new int[]{40, 0, 0, 0});
        eagleResultDto.setWhs(36.0F);
        eagleResultDto.setLastName("Bond");
        eagleResultDto.setFirstName("James");
        eagleResultDto.setSeries(1);


        log.info("Set up each test completed");
    }

    @DisplayName("Should add the new cycle by authorized user")
    @Transactional
    @Test
    void addCycleTest() {

        cycle = cycleService.addCycle(cycle);

        assertNotNull(cycle.getId());
    }

    @DisplayName("Should try to delete tournament from empty cycle")
    @Transactional
    @Test
    void deleteTournamentFromEmptyCycleTest(@Autowired CycleRepository cycleRepository) {

        cycleRepository.save(cycle);

        assertDoesNotThrow(() -> cycleService.removeLastCycleTournament(cycle));
    }

    @DisplayName("Should delete tournament from cycle with single tournament")
    @Transactional
    @Test
    void deleteTournamentFromCycleWithSingleTournamentTest(@Autowired CycleRepository cycleRepository,
                                                           @Autowired CycleTournamentRepository cycleTournamentRepository,
                                                           @Autowired CycleResultRepository cycleResultRepository) {

        cycleRepository.save(cycle);

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("Test cycle tournament");
        cycleTournament.setBestOf(false);
        cycleTournament.setRounds(1);
        cycleTournament.setCycle(cycle);
        cycleTournamentRepository.save(cycleTournament);

        var cycleResult = new CycleResult();
        cycleResult.setResults(new int[]{40, 0, 0, 0});
        cycleResult.setHcp(new String[]{"36.0"});
        cycleResult.setPlayerName("James Bond");
        cycleResult.setCycle(cycle);
        cycleResult.setCycleScore(40);
        cycleResult.setTotal(40);
        cycleResult.setSeries(1);
        cycleResultRepository.save(cycleResult);

        assertDoesNotThrow(() -> cycleService.removeLastCycleTournament(cycle));
        assertEquals(0, cycleResultRepository.findByCycle(cycle).size());
        assertEquals(0, cycleTournamentRepository.findByCycleOrderById(cycle).size());
    }

    @DisplayName("Should delete tournament from cycle with two tournaments")
    @Transactional
    @Test
    void deleteTournamentFromCycleWithTwoTournamentsTest(@Autowired CycleRepository cycleRepository,
                                                           @Autowired CycleTournamentRepository cycleTournamentRepository,
                                                           @Autowired CycleResultRepository cycleResultRepository) {

        cycleRepository.save(cycle);

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("Test cycle tournament");
        cycleTournament.setBestOf(false);
        cycleTournament.setRounds(1);
        cycleTournament.setCycle(cycle);
        cycleTournamentRepository.save(cycleTournament);

        var cycleTournament2 = new CycleTournament();
        cycleTournament2.setName("Test cycle tournament 2");
        cycleTournament2.setBestOf(false);
        cycleTournament2.setRounds(1);
        cycleTournament2.setCycle(cycle);
        cycleTournamentRepository.save(cycleTournament2);

        var cycleResult = new CycleResult();
        cycleResult.setResults(new int[]{40, 0, 0, 0, 30, 0, 0, 0});
        cycleResult.setHcp(new String[]{"36.0", "36.0"});
        cycleResult.setPlayerName("James Bond");
        cycleResult.setCycle(cycle);
        cycleResult.setCycleScore(70);
        cycleResult.setTotal(70);
        cycleResult.setSeries(1);
        cycleResultRepository.save(cycleResult);

        assertDoesNotThrow(() -> cycleService.removeLastCycleTournament(cycle));
        var cycleResult2 = cycleResultRepository.findByCycle(cycle);
        assertEquals(1, cycleResult2.size());
        assertEquals(4, cycleResult2.getFirst().getResults().length);
        assertEquals(40, cycleResult2.getFirst().getCycleScore());
        assertEquals(40, cycleResult2.getFirst().getTotal());
        assertEquals(1, cycleTournamentRepository.findByCycleOrderById(cycle).size());
    }


    @DisplayName("Should add the cycle tournament")
    @Transactional
    @ParameterizedTest
    @CsvSource({
            "false, 1, 36.0, 1",
            "false, 1, 11.0, 1",
            "false, 1, 11.0, 0",
            "true, 1, 11.0, 0",
            "false, 1, 11.0, 1",
    })
    void addCycleTournamentTest(boolean bestOf, int rounds, float whs, int bestRounds) {

        cycle.setBestRounds(bestRounds);
        cycle = cycleService.addCycle(cycle);

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("Test cycle tournament");
        cycleTournament.setBestOf(bestOf);
        cycleTournament.setRounds(rounds);
        cycleTournament.setCycle(cycle);

        eagleResultDto.setWhs(whs);

        cycleTournament = cycleService.addCycleTournament(cycleTournament, new EagleResultDto[]{eagleResultDto});

        assertNotNull(cycleTournament.getId());
    }

    @DisplayName("Get all cycles")
    @Transactional
    @Test
    void getAllCyclesTest() {

        cycleService.addCycle(cycle);

        assertEquals(1, cycleService.findAllCycles().size());
        assertEquals("Test cycle", cycleService.findAllCycles().getFirst().getName());

    }

    @DisplayName("Get all cycle tournaments")
    @Transactional
    @Test
    void getAllCycleTournamentTest() {

        cycle = cycleService.addCycle(cycle);

        var cycleTournament2 = new CycleTournament();
        cycleTournament2.setName("Test cycle tournament 2");
        cycleTournament2.setBestOf(false);
        cycleTournament2.setRounds(1);
        cycleTournament2.setCycle(cycle);
        cycleService.addCycleTournament(cycleTournament2, new EagleResultDto[]{eagleResultDto});

        var cycleTournament1 = new CycleTournament();
        cycleTournament1.setName("Test cycle tournament 1");
        cycleTournament1.setBestOf(false);
        cycleTournament1.setRounds(1);
        cycleTournament1.setCycle(cycle);
        cycleService.addCycleTournament(cycleTournament1, new EagleResultDto[]{eagleResultDto});

        var cycleTournaments = cycleService.findAllCycleTournaments(cycle.getId());

        assertEquals(2, cycleTournaments.size());

    }

    @DisplayName("Get all cycles")
    @Transactional
    @Test
    void getAllCycleResultsTest() {

        assertEquals(0, cycleService.findCycleResults(1L).size());
    }

    @DisplayName("Close cycle by authorized user")
    @Transactional
    @Test
    void closeCycleTest() {

        cycle = cycleService.addCycle(cycle);

        cycleService.closeCycle(cycle.getId());

        cycleService.findAllCycles();

        assertEquals(Cycle.STATUS_CLOSE, cycleService.findAllCycles().getFirst().getStatus());

    }


    @DisplayName("Should delete cycle by authorized user")
    @Transactional
    @Test
    void deleteTest() {

        cycle = cycleService.addCycle(cycle);

        assertDoesNotThrow(() -> cycleService.deleteCycle(cycle.getId()));
    }
}
