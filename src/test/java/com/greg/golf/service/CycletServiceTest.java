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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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
@Testcontainers
class CycleServiceTest {

    @SuppressWarnings("unused")
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Container
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
        eagleResultDto.setWhs(11.0F);
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
        cycleResult.setResults(new Integer[]{40, 0, 0, 0});
        cycleResult.setHcp(new String[]{"36.0"});
        cycleResult.setPlayerName("James Bond");
        cycleResult.setCycle(cycle);
        cycleResult.setCycleScore(40);
        cycleResult.setTotal(40);
        cycleResult.setSeries(1);
        cycleResult.setOldPlace(0);
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
        cycleResult.setResults(new Integer[]{40, 0, 0, 0, 30, 0, 0, 0});
        cycleResult.setHcp(new String[]{"36.0", "36.0"});
        cycleResult.setPlayerName("James Bond");
        cycleResult.setCycle(cycle);
        cycleResult.setCycleScore(70);
        cycleResult.setTotal(70);
        cycleResult.setSeries(1);
        cycleResult.setOldPlace(0);
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

    @DisplayName("Should set oldPlace=0 for all players when first tournament is added")
    @Transactional
    @Test
    void addFirstTournamentSetsOldPlaceZeroTest() {

        cycle = cycleService.addCycle(cycle);

        var cycleTournament = new CycleTournament();
        cycleTournament.setName("First tournament");
        cycleTournament.setBestOf(false);
        cycleTournament.setRounds(1);
        cycleTournament.setCycle(cycle);

        cycleService.addCycleTournament(cycleTournament, new EagleResultDto[]{eagleResultDto});

        var results = cycleService.findCycleResults(cycle.getId());
        assertFalse(results.isEmpty());
        results.forEach(r -> assertEquals(0, r.getOldPlace(),
                "oldPlace should be 0 for every player after the first tournament"));
    }

    @DisplayName("Should record previous place when second tournament is added")
    @Transactional
    @Test
    void addSecondTournamentSetsOldPlaceTest() {

        cycle.setBestRounds(0);
        cycle = cycleService.addCycle(cycle);

        // first tournament: player 1 scores 40, player 2 scores 30
        var t1 = new CycleTournament();
        t1.setName("Tournament 1");
        t1.setBestOf(false);
        t1.setRounds(1);
        t1.setCycle(cycle);

        var player2 = new EagleResultDto();
        player2.setR(new int[]{30, 0, 0, 0});
        player2.setWhs(11.0F);
        player2.setLastName("Doe");
        player2.setFirstName("John");
        player2.setSeries(1);

        cycleService.addCycleTournament(t1, new EagleResultDto[]{eagleResultDto, player2});

        // after first tournament: Bond rank=1 (40pts), Doe rank=2 (30pts)
        var afterFirst = cycleService.findCycleResults(cycle.getId());
        afterFirst.forEach(r -> assertEquals(0, r.getOldPlace(),
                "oldPlace should be 0 after the first tournament"));

        // second tournament: player 2 scores 50, player 1 scores 10
        var t2 = new CycleTournament();
        t2.setName("Tournament 2");
        t2.setBestOf(false);
        t2.setRounds(1);
        t2.setCycle(cycle);

        var bondT2 = new EagleResultDto();
        bondT2.setR(new int[]{10, 0, 0, 0});
        bondT2.setWhs(11.0F);
        bondT2.setLastName("Bond");
        bondT2.setFirstName("James");
        bondT2.setSeries(1);

        var doeT2 = new EagleResultDto();
        doeT2.setR(new int[]{50, 0, 0, 0});
        doeT2.setWhs(11.0F);
        doeT2.setLastName("Doe");
        doeT2.setFirstName("John");
        doeT2.setSeries(1);

        cycleService.addCycleTournament(t2, new EagleResultDto[]{bondT2, doeT2});

        // after second tournament results are sorted by cycleScore DESC: Doe=80, Bond=50
        var afterSecond = cycleService.findCycleResults(cycle.getId());
        assertEquals(2, afterSecond.size());

        // find each player by name
        var bondResult = afterSecond.stream().filter(r -> r.getPlayerName().contains("Bond")).findFirst().orElseThrow();
        var doeResult  = afterSecond.stream().filter(r -> r.getPlayerName().contains("Doe")).findFirst().orElseThrow();

        // Bond was rank 1 before second tournament
        assertEquals(1, bondResult.getOldPlace(), "Bond should have oldPlace=1");
        // Doe was rank 2 before second tournament
        assertEquals(2, doeResult.getOldPlace(), "Doe should have oldPlace=2");
    }

    @DisplayName("Should set oldPlace=0 for all players after tournament deletion")
    @Transactional
    @Test
    void deleteTournamentResetsOldPlaceTest(@Autowired CycleRepository cycleRepository,
                                            @Autowired CycleTournamentRepository cycleTournamentRepository,
                                            @Autowired CycleResultRepository cycleResultRepository) {

        cycleRepository.save(cycle);

        var t1 = new CycleTournament();
        t1.setName("Tournament A");
        t1.setBestOf(false);
        t1.setRounds(1);
        t1.setCycle(cycle);
        cycleTournamentRepository.save(t1);

        var t2 = new CycleTournament();
        t2.setName("Tournament B");
        t2.setBestOf(false);
        t2.setRounds(1);
        t2.setCycle(cycle);
        cycleTournamentRepository.save(t2);

        var cr = new CycleResult();
        cr.setResults(new Integer[]{40, 0, 0, 0, 30, 0, 0, 0});
        cr.setHcp(new String[]{"11.0", "11.0"});
        cr.setPlayerName("James Bond");
        cr.setCycle(cycle);
        cr.setCycleScore(70);
        cr.setTotal(70);
        cr.setSeries(1);
        cr.setOldPlace(1);
        cycleResultRepository.save(cr);

        cycleService.removeLastCycleTournament(cycle);

        var results = cycleResultRepository.findByCycle(cycle);
        assertEquals(1, results.size());
        assertEquals(0, results.getFirst().getOldPlace(),
                "oldPlace should be reset to 0 after tournament deletion");
    }

    @DisplayName("Should compute stroke play oldPlace using the achieved/not-achieved ordering")
    @Transactional
    @Test
    void strokePlayOldPlaceRespectsBestRoundsPartitionTest() {

        // stroke play (series 2): lower strokes are better; require 3 best rounds
        cycle.setBestRounds(3);
        cycle.setSeries(2);
        cycle = cycleService.addCycle(cycle);

        // Tournament 1: Alpha 10, Beta 20 (both played 1 round -> not achieved)
        addStrokePlayTournament("Tour1", strokePlayPlayer("Alpha", "A", 10),
                strokePlayPlayer("Beta", "B", 20));

        // Tournament 2: Alpha 10, Beta 20 (both played 2 rounds -> still not achieved)
        addStrokePlayTournament("Tour2", strokePlayPlayer("Alpha", "A", 10),
                strokePlayPlayer("Beta", "B", 20));

        // Tournament 3: only Beta plays 5 (Beta played 3 -> achieved, Alpha still played 2)
        addStrokePlayTournament("Tour3", strokePlayPlayer("Beta", "B", 5));

        // Before Tournament 4 the standings are: Beta (achieved) rank 1, Alpha (not achieved) rank 2,
        // even though Alpha has fewer cumulative strokes. This is the case the fix must honour.
        // Tournament 4: only Alpha plays 5 (Alpha played 3 -> achieved)
        addStrokePlayTournament("Tour4", strokePlayPlayer("Alpha", "A", 5));

        var results = cycleService.findCycleResults(cycle.getId());
        var alpha = results.stream().filter(r -> r.getPlayerName().contains("Alpha")).findFirst().orElseThrow();
        var beta  = results.stream().filter(r -> r.getPlayerName().contains("Beta")).findFirst().orElseThrow();

        // Before T4: Beta was displayed first (achieved), Alpha second (not achieved)
        assertEquals(1, beta.getOldPlace(), "Beta was displayed in position 1 before the last tournament");
        assertEquals(2, alpha.getOldPlace(), "Alpha was displayed in position 2 before the last tournament");
    }

    private EagleResultDto strokePlayPlayer(String lastName, String firstName, int strokes) {
        var dto = new EagleResultDto();
        dto.setR(new int[]{strokes, 0, 0, 0});
        dto.setWhs(11.0F);
        dto.setLastName(lastName);
        dto.setFirstName(firstName);
        dto.setSeries(2);
        return dto;
    }

    private void addStrokePlayTournament(String name, EagleResultDto... players) {
        var tournament = new CycleTournament();
        tournament.setName(name);
        tournament.setBestOf(false);
        tournament.setRounds(1);
        tournament.setCycle(cycle);
        cycleService.addCycleTournament(tournament, players);
    }
}
