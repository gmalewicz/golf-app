package com.greg.golf.service;

import com.greg.golf.entity.*;

import com.greg.golf.repository.*;
import com.greg.golf.service.helpers.RoleVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceDeleteTournamentRoundTest {

    @Mock
    private TournamentResultRepository tournamentResultRepository;
    @Mock
    private TournamentRoundRepository tournamentRoundRepository;
    @Mock
    private RoundService roundService;
    @Mock
    private PlayerRoundRepository playerRoundRepository;
    @Mock
    TournamentService selfMock;

    @Spy
    @InjectMocks
    private TournamentService tournamentService;

    // We need to mock static method RoleVerification.verifyPlayer, use Mockito mockStatic

    private static final Long TOURNAMENT_RESULT_ID = 1L;
    private static final Integer ROUND_ID = 2;

    private TournamentResult tournamentResult;
    private Tournament tournament;
    private TournamentRound tournamentRound;
    private PlayerRound playerRound;
    private Player player;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        tournament = new Tournament();
        tournament.setId(10L);
        tournament.setBestRounds(3);
        tournament.setPlayer(new Player());
        tournament.getPlayer().setId(100L);

        player = new Player();
        player.setId(200L);

        tournamentResult = new TournamentResult();
        tournamentResult.setId(TOURNAMENT_RESULT_ID);
        tournamentResult.setTournament(tournament);
        tournamentResult.setPlayer(player);
        tournamentResult.setPlayedRounds(4);
        tournamentResult.setStbGross(50);
        tournamentResult.setStbNet(40);
        tournamentResult.setStrokesBrutto(100);
        tournamentResult.setStrokesNetto(90);

        tournamentRound = new TournamentRound();
        tournamentRound.setRoundId(ROUND_ID);
        tournamentRound.setStbGross(10);
        tournamentRound.setStbNet(8);
        tournamentRound.setStrokesBrutto(20);
        tournamentRound.setStrokesNetto(18);
        tournamentRound.setTournamentResult(tournamentResult);

        playerRound = new PlayerRound();
        playerRound.setId(5L);
        playerRound.setPlayerId(player.getId());
        playerRound.setTournamentId(tournament.getId());

        // Inject mocked self into the tournamentService self field
        Field selfField = TournamentService.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(tournamentService, selfMock);
    }

    @Test
    void deleteTournamentRound_roundNotFound_nothingDeleted() {
        // Setup repository to return tournamentResult
        when(tournamentResultRepository.findById(TOURNAMENT_RESULT_ID)).thenReturn(Optional.of(tournamentResult));

        // Setup getTournamentRoundsForResult to return empty list (no rounds found)
        doReturn(Collections.emptyList()).when(selfMock).getTournamentRoundsForResult(TOURNAMENT_RESULT_ID);

        // Mock static RoleVerification.verifyPlayer
        try (MockedStatic<RoleVerification> mockedRoleVerification = mockStatic(RoleVerification.class)) {
            // Act
            tournamentService.deleteTournamentRound(TOURNAMENT_RESULT_ID, ROUND_ID);

            // Verify delete is never called because no rounds found with that roundId
            verify(tournamentRoundRepository, never()).delete(any());
            verify(playerRoundRepository, never()).save(any());
            verify(tournamentResultRepository, never()).save(any());

            mockedRoleVerification.verify(() ->
                    RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament round by unauthorized user"));
        }
    }

    @Test
    void deleteTournamentRound_roundFound_andPlayedRoundsGreaterThanBestRounds_updatesTotals() {
        // Setup repository to return tournamentResult
        when(tournamentResultRepository.findById(TOURNAMENT_RESULT_ID)).thenReturn(Optional.of(tournamentResult));

        // Setup getTournamentRoundsForResult to return list with tournamentRound having roundId
        LinkedList<TournamentRound> rounds = new LinkedList<>();
        rounds.add(tournamentRound);
        doReturn(rounds).when(selfMock).getTournamentRoundsForResult(TOURNAMENT_RESULT_ID);

        // Setup roundService to return playerRound when requested
        when(roundService.getForPlayerRoundDetails(player.getId(), ROUND_ID.longValue())).thenReturn(playerRound);

        // Setup playerRoundRepository save to return playerRound
        when(playerRoundRepository.save(any(PlayerRound.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Setup tournamentResultRepository save to return tournamentResult
        when(tournamentResultRepository.save(any(TournamentResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock static RoleVerification.verifyPlayer
        try (MockedStatic<RoleVerification> mockedRoleVerification = mockStatic(RoleVerification.class)) {
            // Act
            tournamentService.deleteTournamentRound(TOURNAMENT_RESULT_ID, ROUND_ID);

            // Verify tournamentRoundRepository.delete called once with the round
            verify(tournamentRoundRepository, times(1)).delete(tournamentRound);

            // Verify playerRound's tournamentId is set to null and saved
            assertNull(playerRound.getTournamentId());
            verify(playerRoundRepository, times(1)).save(playerRound);

            // Verify tournamentResult totals are updated correctly
            assertEquals(50 - 10, tournamentResult.getStbGross());
            assertEquals(40 - 8, tournamentResult.getStbNet());
            assertEquals(100 - 20, tournamentResult.getStrokesBrutto());
            assertEquals(90 - 18, tournamentResult.getStrokesNetto());
            assertEquals(3, tournamentResult.getPlayedRounds());

            // Verify tournamentResultRepository save called
            verify(tournamentResultRepository, times(1)).save(tournamentResult);

            mockedRoleVerification.verify(() ->
                    RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament round by unauthorized user"));
        }
    }

    @Test
    void deleteTournamentRound_roundFound_andPlayedRoundsLessThanBestRounds_updatesPlayedRounds() {

        tournamentResult.setPlayedRounds(4);
        tournament.setBestRounds(3);

        when(tournamentResultRepository.findById(TOURNAMENT_RESULT_ID)).thenReturn(Optional.of(tournamentResult));

        LinkedList<TournamentRound> rounds = new LinkedList<>();
        rounds.add(tournamentRound);
        doReturn(rounds).when(selfMock).getTournamentRoundsForResult(TOURNAMENT_RESULT_ID);

        when(roundService.getForPlayerRoundDetails(player.getId(), ROUND_ID.longValue())).thenReturn(playerRound);
        when(playerRoundRepository.save(any(PlayerRound.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<RoleVerification> mockedRoleVerification = mockStatic(RoleVerification.class)) {

            int playedRoundsBefore = tournamentResult.getPlayedRounds();

            tournamentService.deleteTournamentRound(TOURNAMENT_RESULT_ID, ROUND_ID);

            verify(tournamentRoundRepository, times(1)).delete(tournamentRound);

            assertNull(playerRound.getTournamentId());
            verify(playerRoundRepository, times(1)).save(playerRound);


            assertNotEquals(playedRoundsBefore, tournamentResult.getPlayedRounds(),
                    "Expected playedRounds to be updated by updateForBestRounds");

            mockedRoleVerification.verify(() ->
                    RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament round by unauthorized user"));
        }
    }

    @Test
    void deleteTournamentRound_tournamentResultNotFound_throwsException() {
        // Setup repository to return empty
        when(tournamentResultRepository.findById(TOURNAMENT_RESULT_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> tournamentService.deleteTournamentRound(TOURNAMENT_RESULT_ID, ROUND_ID));
    }
}
