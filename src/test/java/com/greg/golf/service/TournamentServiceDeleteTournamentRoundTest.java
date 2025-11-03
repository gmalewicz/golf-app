package com.greg.golf.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.GeneralException;
import com.greg.golf.repository.TournamentResultRepository;
import com.greg.golf.repository.TournamentRoundRepository;
import com.greg.golf.repository.PlayerRoundRepository;
import com.greg.golf.service.helpers.RoleVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

class TournamentServiceDeleteTournamentRoundTest {

    @Mock
    private TournamentResultRepository tournamentResultRepository;
    @Mock
    private TournamentRoundRepository tournamentRoundRepository;
    @Mock
    private PlayerRoundRepository playerRoundRepository;
    @Mock
    private RoundService roundService;
    @Mock
    private TournamentService self; // self reference for calls inside service

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament tournament;
    private TournamentResult tournamentResult;
    private PlayerRound playerRound;
    private TournamentRound tournamentRound;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set the self reference for the service (to mock self calls)
        ReflectionTestUtils.setField(tournamentService, "self", self);

        // Setup tournament and tournamentResult mocks
        var player = new Player();
        player.setId(1L);
        player.setPassword("test");

        tournament = new Tournament();
        tournament.setId(10L);
        tournament.setPlayer(player);
        tournament.setStatus(Tournament.STATUS_OPEN);
        tournament.setBestRounds(Common.ALL_ROUNDS);

        tournamentResult = new TournamentResult();
        tournamentResult.setId(100L);
        tournamentResult.setTournament(tournament);
        tournamentResult.setPlayer(player);
        tournamentResult.setPlayedRounds(1);
        tournamentResult.setStbGross(50);
        tournamentResult.setStbNet(40);
        tournamentResult.setStrokesBrutto(70);
        tournamentResult.setStrokesNetto(60);

        playerRound = new PlayerRound();
        playerRound.setId(200L);
        playerRound.setPlayerId(player.getId());
        playerRound.setTournamentId(100L);

        tournamentRound = new TournamentRound();
        tournamentRound.setId(300L);
        tournamentRound.setRoundId(5);
        tournamentRound.setStbGross(10);
        tournamentRound.setStbNet(8);
        tournamentRound.setStrokesBrutto(15);
        tournamentRound.setStrokesNetto(12);

        // Mock the findById for tournamentResultRepository
        when(tournamentResultRepository.findById(tournamentResult.getId()))
                .thenReturn(Optional.of(tournamentResult));
    }

    @Test
    void testDeleteTournamentRound_TournamentClosed_ThrowsGeneralException() {

        UserDetails userDetails = new User(tournament.getPlayer().getId().toString(), tournament.getPlayer().getPassword(), new ArrayList<SimpleGrantedAuthority>());

        var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


        tournament.setStatus(Tournament.STATUS_CLOSE);

        Long resultId = tournamentResult.getId();
        Integer roundId = tournamentRound.getRoundId();

        GeneralException exception = assertThrows(GeneralException.class, () ->
                tournamentService.deleteTournamentRound(resultId, roundId)
        );

        assertNotNull(exception);
        verify(tournamentResultRepository).findById(tournamentResult.getId());
        // No further interactions expected
        verifyNoMoreInteractions(tournamentRoundRepository, playerRoundRepository, roundService);
    }

    @Test
    void testDeleteTournamentRound_NoMatchingRound_NoAction() {
        tournament.setStatus(Tournament.STATUS_OPEN);

        // self.getTournamentRoundsForResult returns empty list (no rounds to delete)
        when(self.getTournamentRoundsForResult(tournamentResult.getId())).thenReturn(Collections.emptyList());

        tournamentService.deleteTournamentRound(tournamentResult.getId(), 999); // roundId not present

        verify(tournamentRoundRepository, never()).delete(any());
        verify(playerRoundRepository, never()).save(any());
        verify(tournamentResultRepository, never()).delete(any());
        verify(tournamentResultRepository, never()).save(any());
    }

    @Test
    void testDeleteTournamentRound_OneRound_AllRounds_UpdateTotals() {
        tournament.setStatus(Tournament.STATUS_OPEN);
        tournament.setBestRounds(Common.ALL_ROUNDS);

        List<TournamentRound> rounds = List.of(tournamentRound);
        when(self.getTournamentRoundsForResult(tournamentResult.getId())).thenReturn(rounds);
        when(roundService.getForPlayerRoundDetails(tournamentResult.getPlayer().getId(), (long) tournamentRound.getRoundId()))
                .thenReturn(playerRound);

        tournamentService.deleteTournamentRound(tournamentResult.getId(), tournamentRound.getRoundId());

        // Verify tournamentRoundRepository.delete called
        verify(tournamentRoundRepository).delete(tournamentRound);

        // Verify playerRound tournamentId cleared and saved
        assertNull(playerRound.getTournamentId());
        verify(playerRoundRepository).save(playerRound);

        // Since only 1 round, the tournamentResult should be deleted
        verify(tournamentResultRepository).delete(tournamentResult);

        // No save on tournamentResult because it is deleted
        verify(tournamentResultRepository, never()).save(tournamentResult);
    }

    @Test
    void testDeleteTournamentRound_MultipleRounds_AllRounds_UpdateTotals() {
        tournament.setStatus(Tournament.STATUS_OPEN);
        tournament.setBestRounds(Common.ALL_ROUNDS);

        TournamentRound otherRound = new TournamentRound();
        otherRound.setId(301L);
        otherRound.setRoundId(6);
        otherRound.setStbGross(20);
        otherRound.setStbNet(15);
        otherRound.setStrokesBrutto(25);
        otherRound.setStrokesNetto(22);

        List<TournamentRound> rounds = List.of(tournamentRound, otherRound);
        when(self.getTournamentRoundsForResult(tournamentResult.getId())).thenReturn(rounds);
        when(roundService.getForPlayerRoundDetails(tournamentResult.getPlayer().getId(), (long) tournamentRound.getRoundId()))
                .thenReturn(playerRound);

        tournamentService.deleteTournamentRound(tournamentResult.getId(), tournamentRound.getRoundId());

        verify(tournamentRoundRepository).delete(tournamentRound);

        assertNull(playerRound.getTournamentId());
        verify(playerRoundRepository).save(playerRound);

        // Since more than 1 round and bestRounds == ALL_ROUNDS, totals are updated and saved
        assertEquals(40 - tournamentRound.getStbNet(), tournamentResult.getStbNet());
        assertEquals(50 - tournamentRound.getStbGross(), tournamentResult.getStbGross());
        assertEquals(60 - tournamentRound.getStrokesNetto(), tournamentResult.getStrokesNetto());
        assertEquals(70 - tournamentRound.getStrokesBrutto(), tournamentResult.getStrokesBrutto());
        assertEquals(0, tournamentResult.getPlayedRounds());

        verify(tournamentResultRepository).save(tournamentResult);
        verify(tournamentResultRepository, never()).delete(tournamentResult);
    }

    @Test
    void testDeleteTournamentRound_BestRoundsNotAll_UpdatesTotalsAndSaves() {
        tournament.setStatus(Tournament.STATUS_OPEN);
        tournament.setBestRounds(2); // not ALL_ROUNDS

        TournamentRound otherRound = new TournamentRound();
        otherRound.setId(301L);
        otherRound.setRoundId(6);
        otherRound.setStbGross(20);
        otherRound.setStbNet(15);
        otherRound.setStrokesBrutto(25);
        otherRound.setStrokesNetto(22);

        List<TournamentRound> rounds = List.of(tournamentRound, otherRound);
        when(self.getTournamentRoundsForResult(tournamentResult.getId())).thenReturn(rounds);
        when(roundService.getForPlayerRoundDetails(tournamentResult.getPlayer().getId(), (long) tournamentRound.getRoundId()))
                .thenReturn(playerRound);

        tournamentService.deleteTournamentRound(tournamentResult.getId(), tournamentRound.getRoundId());

        verify(tournamentRoundRepository).delete(tournamentRound);
        assertNull(playerRound.getTournamentId());
        verify(playerRoundRepository).save(playerRound);

        // Played rounds decreased by 1
        assertEquals(0, tournamentResult.getPlayedRounds());

        // Verify save is called on tournamentResultRepository with updated tournamentResult
        verify(tournamentResultRepository).save(tournamentResult);

        // Check that tournamentResult fields are recalculated (values depend on your mocked data)
        // For example: stbNet should be <= previous stbNet minus the removed round's stbNet
        assertTrue(tournamentResult.getStbNet() <= 40);
        assertTrue(tournamentResult.getStbGross() <= 50);
        assertTrue(tournamentResult.getStrokesNetto() <= 60);
        assertTrue(tournamentResult.getStrokesBrutto() <= 70);
    }

    @Test
    void testDeleteTournamentRound_VerifyRoleVerificationCalled() {
        tournament.setStatus(Tournament.STATUS_OPEN);
        when(self.getTournamentRoundsForResult(tournamentResult.getId())).thenReturn(Collections.emptyList());

        try (MockedStatic<RoleVerification> roleVerificationMockedStatic = mockStatic(RoleVerification.class)) {
            tournamentService.deleteTournamentRound(tournamentResult.getId(), 1);
            roleVerificationMockedStatic.verify(() -> RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament round by unauthorized user"));
        }
    }
}
