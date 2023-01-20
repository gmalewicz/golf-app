package com.greg.golf.service;

import java.util.*;
import java.util.stream.Collectors;

import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.DeleteTournamentPlayerException;
import com.greg.golf.error.DuplicatePlayerInTournamentException;
import com.greg.golf.repository.*;
import com.greg.golf.service.helpers.RoleVerification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.error.RoundAlreadyAddedToTournamentException;
import com.greg.golf.error.TooFewHolesForTournamentException;
import com.greg.golf.service.events.RoundEvent;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service("tournamentService")
@RequiredArgsConstructor
public class TournamentService {

    private static final int TOURNAMENT_HOLES = 18;

    private final TournamentResultRepository tournamentResultRepository;
    private final TournamentRepository tournamentRepository;
    private final RoundService roundService;
    private final CourseService courseService;
    private final PlayerRoundRepository playerRoundRepository;
    private final TournamentRoundRepository tournamentRoundRepository;
    private final RoundRepository roundRepository;
    private final PlayerRepository playerRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public void deleteTournament(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // then verify if player is allowed to delete result
        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament result by unauthorized user");

        tournament
            .getTournamentResult()
            .stream()
            .map(TournamentResult::getId)
            .collect(Collectors.toList())
            .forEach(this::deleteResult);

        tournamentRepository.deleteById(tournamentId);
    }


    @Transactional
    public void deleteResult(Long resultId) {

        log.debug("resultId");

        // first get the object
        var tournamentResult = tournamentResultRepository.findById(resultId).orElseThrow();
        // then verify if player is allowed to delete result
        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournamentResult.getTournament().getPlayer().getId(), "Attempt to delete tournament result by unauthorized user");
        // then clear tournament flag for rounds
        tournamentResult.getTournamentRound().forEach(tournamentRnd -> {

            if (tournamentRnd.getRoundId() != null) {

                var round = roundRepository.findById(tournamentRnd.getRoundId().longValue()).orElseThrow();
                round.setTournament(null);
                roundRepository.save(round);
            }

        });

        // then clear tournament flag for player round
        playerRoundRepository.clearTournamentForPlayer(tournamentResult.getPlayer().getId(), tournamentResult.getTournament().getId());

        // then delete result
        var rstLst = tournamentResult.getTournament().getTournamentResult()
                                            .stream()
                                            .filter(rst -> rst.getId().equals(resultId))
                                            .collect(Collectors.toList());
        tournamentResult.getTournament().getTournamentResult().removeAll(rstLst);
        tournamentRepository.save(tournamentResult.getTournament());
    }

    @Transactional
    public List<Tournament> findAllTournaments() {
        return tournamentRepository.findAll(Sort.by(Sort.Direction.DESC, "endDate"));
    }

    @Transactional
    public List<TournamentResult> findAllTournamentsResults(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // in case if all rounds shall be included return all results sorted by player rounds and stb net
        if (tournament.getBestRounds() == Common.ALL_ROUNDS) {
            return tournamentResultRepository.findByTournamentOrderByPlayedRoundsDescStbNetDesc(tournament);
        }

        // in case of bestRounds perform 2 queries and join them
        var retList =
                tournamentResultRepository.findByTournamentAndPlayedRoundsGreaterThanEqualOrderByStbNetDesc(
                        tournament, tournament.getBestRounds());

        retList.addAll(tournamentResultRepository
                .findByTournamentAndPlayedRoundsLessThanOrderByPlayedRoundsDescStbNetDesc(tournament, tournament.getBestRounds()));

        return retList;
    }

    @Transactional
    public Tournament addTournament(Tournament tournament) {

        // just to make the time adjustment
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(tournament.getEndDate());
        calendar.roll(Calendar.DATE, 1);
        calendar.roll(Calendar.HOUR, 23);

        tournament.setEndDate(calendar.getTime());
        tournament.setStatus(Tournament.STATUS_OPEN);

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public TournamentRound addRoundOnBehalf(Long tournamentId, Round round) {

        round = roundService.saveRound(round);
        entityManager.detach(round);

        var tournamentLst = addRound(tournamentId, round.getId(), true);

        //it must be one and only one result
        if (tournamentLst == null || tournamentLst.size() != 1) {
            return null;
        }

        return tournamentLst.get(0);
    }

    @Transactional
    public List<TournamentRound> addRound(Long tournamentId, Long roundId, boolean updateResults) {

        // first find the round in database
        var round = roundService.getWithPlayers(roundId).orElseThrow();

        // get tournament object
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // next check if round is not already added to that tournament
        if (tournament.getRound().contains(round)) {
            log.warn("Attempt to add twice the same round to tournament");
            throw new RoundAlreadyAddedToTournamentException();
        }

        // add round to tournament
        tournament.addRound(round);
        tournamentRepository.save(tournament);

        tournament = tournamentRepository.findById(tournament.getId()).orElseThrow();

        // update tournament result
        if (updateResults) {
            return updateTournamentResult(round, tournament);
        }
        return new ArrayList<>();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @EventListener
    public void handleRoundEvent(RoundEvent roundEvent) {
        log.debug("Handling round event...");
        updateTournamentResult(roundEvent.getRound(), roundEvent.getRound().getTournament());
    }

    @Transactional
    public List<TournamentRound> updateTournamentResult(Round round, Tournament tournament) {

        var tournamentRoundLst = new ArrayList<TournamentRound>();

        // first verify if round has 18 holes played for each player
        verifyRoundCorrectness(round);

        // iterate through round players and check if they already added
        round.getPlayer().forEach(player -> {

            var playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());

            Optional<TournamentResult> tournamentResultOpt = tournamentResultRepository
                    .findByPlayerAndTournament(player, round.getTournament());
            tournamentResultOpt.ifPresentOrElse(tournamentResult -> {
                log.debug("Attempting to update tournament result");
                // first check if the round has not been already added
                if (playerRound.getTournamentId() == null) {
                    tournamentResult.setPlayedRounds(tournamentResult.getPlayedRounds() + 1);
                    int grossStrokes = 0;
                    int netStrokes = 0;
                    List<Integer> stb = updateSTB(tournamentResult, round, playerRound, player);
                    // check if round is applicable for stroke statistic
                    boolean strokeApplicable = applicableForStroke(round, player);
                    if (strokeApplicable) {
                        tournamentResult.increaseStrokeRounds();
                        grossStrokes = getGrossStrokes(player, round);
                        netStrokes = getNetStrokes(player, round, grossStrokes, playerRound);
                    }
                    tournamentResult.setStrokesBrutto(tournamentResult.getStrokesBrutto() + grossStrokes);
                    tournamentResult.setStrokesNetto(tournamentResult.getStrokesNetto() + netStrokes);

                    // save entity
                    tournamentResultRepository.save(tournamentResult);
                    tournamentRoundLst.add(addTournamentRound(stb.get(1), stb.get(0), grossStrokes, netStrokes,
                            getScoreDifferential(playerRound, round, player), round.getCourse().getName(),
                            tournamentResult, strokeApplicable, round.getId()));

                    // here needs to be an update of TournamentResults in case if number of added rounds is greater
                    // than bestRounds assuming that bestRounds is not 0
                    updateForBestRounds(tournament, tournamentResult);

                } else {
                    log.warn("Attempt to update round which is already part of the tournament");
                }

            }, () -> {
                log.debug("Attempting to add the new round to tournament result");
                // if it is the first record to be added to result than create it
                var tournamentResult = buildEmptyTournamentResult(player);
                tournamentResult.setTournament(round.getTournament());
                // update stb results
                List<Integer> stb = updateSTB(tournamentResult, round, playerRound, player);
                // check if round is applicable for stroke statistic
                boolean strokeApplicable = applicableForStroke(round, player);
                if (strokeApplicable) {
                    tournamentResult.increaseStrokeRounds();
                    // get gross and net strokes
                    tournamentResult.setStrokesBrutto(getGrossStrokes(player, round));
                    tournamentResult.setStrokesNetto(
                            getNetStrokes(player, round, tournamentResult.getStrokesBrutto(), playerRound));
                } else {
                    tournamentResult.setStrokesBrutto(0);
                    tournamentResult.setStrokesNetto(0);
                }
                // save entity
                tournamentResultRepository.save(tournamentResult);

                tournamentRoundLst.add(addTournamentRound(stb.get(1), stb.get(0), tournamentResult.getStrokesBrutto(),
                        tournamentResult.getStrokesNetto(), getScoreDifferential(playerRound, round, player),
                        round.getCourse().getName(), tournamentResult, strokeApplicable, round.getId()));

            });

            // set tournament id in player_round
            playerRound.setTournamentId(round.getTournament().getId());
            playerRoundRepository.save(playerRound);
        });

        return tournamentRoundLst;
    }

    private void updateForBestRounds(Tournament tournament, TournamentResult tournamentResult) {

        // skip processing if all rounds shall be included
        if (tournament.getBestRounds() == Common.ALL_ROUNDS) {
            log.debug("Skipping as all rounds should be included");
            return;
        }
        // get all rounds
        List<TournamentRound> tournamentRoundList =
                tournamentRoundRepository.findByTournamentResultOrderByIdAsc(tournamentResult);
        // skip processing if number of rounds is lower equal bestRounds
        if (tournamentRoundList.size() <= tournament.getBestRounds()) {
            log.debug("Skipping as number of rounds is lower or equals than number of best rounds");
            return;
        }
        log.debug("All checks done - beginning of updating tournament result");

        // calculate stb net - more the better
        tournamentResult.setStbNet(
                tournamentRoundList
                        .stream()
                        .mapToInt(TournamentRound::getStbNet)
                        .boxed()
                        .sorted(Comparator.reverseOrder())
                        .limit(tournament.getBestRounds())
                        .reduce(0, Integer::sum));

        // calculate stb gross - more the better
        tournamentResult.setStbGross(
                tournamentRoundList
                        .stream()
                        .mapToInt(TournamentRound::getStbGross)
                        .boxed()
                        .sorted(Comparator.reverseOrder())
                        .limit(tournament.getBestRounds())
                        .reduce(0, Integer::sum));

        // calculate net strokes - lower the better
        tournamentResult.setStrokesNetto(
                tournamentRoundList
                        .stream()
                        .filter(TournamentRound::getStrokes)
                        .mapToInt(TournamentRound::getStrokesNetto)
                        .boxed()
                        .sorted()
                        .limit(tournament.getBestRounds())
                        .reduce(0, Integer::sum));

        // calculate gross strokes - lower the better
        tournamentResult.setStrokesBrutto(
                tournamentRoundList
                        .stream()
                        .filter(TournamentRound::getStrokes)
                        .mapToInt(TournamentRound::getStrokesBrutto)
                        .boxed()
                        .sorted()
                        .limit(tournament.getBestRounds())
                        .reduce(0, Integer::sum));

        // update tournament results in database
        log.debug("Update tournament results in database");
        tournamentResultRepository.save(tournamentResult);
    }

    @SuppressWarnings("java:S107")
    @Transactional
    public TournamentRound addTournamentRound(int stbGross, int stbNet, int strokesGross, int strokesNet, float scrDiff,
                                              String courseName, TournamentResult tournamentResult, boolean strokeApplicable,
                                              long roundId) {

        var tournamentRound = new TournamentRound();
        tournamentRound.setCourseName(courseName);
        tournamentRound.setScrDiff(scrDiff);
        tournamentRound.setStbGross(stbGross);
        tournamentRound.setStbNet(stbNet);
        tournamentRound.setStrokesBrutto(strokesGross);
        tournamentRound.setStrokesNetto(strokesNet);
        tournamentRound.setTournamentResult(tournamentResult);
        tournamentRound.setStrokes(strokeApplicable);
        tournamentRound.setRoundId((int)roundId);

        tournamentRound = tournamentRoundRepository.save(tournamentRound);

        return tournamentRound;
    }

    @Transactional
    public List<TournamentRound> getTournamentRoundsForResult(Long resultId) {

        var tr = new TournamentResult();
        tr.setId(resultId);

        return tournamentRoundRepository.findByTournamentResultOrderByIdAsc(tr);

    }

    // calculate score differential
    @Transactional
    public float getScoreDifferential(PlayerRound playerRound, Round round, Player player) {

        return (113 / (float) playerRound.getSr()) * (getCorrectedStrokes(player, round) - playerRound.getCr());

    }

    // calculate gross strokes
    @Transactional
    public int getGrossStrokes(Player player, Round round) {

        log.debug("player: " + player);
        log.debug("round: " + round);

        // calculate gross result
        int grossStrokes = round.getScoreCard().stream().filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()))
                .mapToInt(ScoreCard::getStroke).sum();
        log.debug("Calculated gross strokes: " + grossStrokes);
        return grossStrokes;
    }

    // calculate gross strokes
    @Transactional
    public boolean applicableForStroke(Round round, Player player) {

        log.debug("Start checking round for stroke statistic");

        //search for 16 which means that hole has been given up
        return round.getScoreCard()
                .stream()
                .filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()))
                .noneMatch(scoreCard -> scoreCard.getStroke() >= Common.HOLE_GIVEN_UP);
    }

    // calculate corrected strokes
    @Transactional
    public int getCorrectedStrokes(Player player, Round round) {

        log.debug("player: " + player);
        log.debug("round: " + round);

        List<Hole> holes = round.getCourse().getHoles();

        // calculate gross result
        int grossStrokes = round.getScoreCard().stream().filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()))
                .mapToInt(scoreCard -> {

                    if ((scoreCard.getHcp() + 2 + holes.get(scoreCard.getHole() - 1).getPar()) < scoreCard
                            .getStroke()) {
                        return scoreCard.getHcp() + 2 + holes.get(scoreCard.getHole() - 1).getPar();
                    } else {
                        return scoreCard.getStroke();
                    }

                }).sum();

        log.debug("Calculated corrected strokes: " + grossStrokes);
        return grossStrokes;
    }

    // calculate net strokes
    @Transactional
    public int getNetStrokes(Player player, Round round, int grossStrokes, PlayerRound playerRound) {

        // calculate course HCP
        int courseHCP = getCourseHCP(playerRound, round, player);

        int netStrokes = grossStrokes - courseHCP;
        if (netStrokes < 0) {
            netStrokes = 0;
        }

        log.debug("Calculated net strokes: " + netStrokes);
        return netStrokes;
    }

    // returns STB net at index 0 and STB gross at index 1
    @Transactional
    public List<Integer> updateSTB(TournamentResult tournamentResult, Round round, PlayerRound playerRound,
                                   Player player) {

        // create List of ret values
        List<Integer> retStb = new ArrayList<>();

        // calculate course HCP
        int courseHCP = getCourseHCP(playerRound, round, player);

        // calculate hole HCP for player
        int hcpAll = (int) Math.floor((double) courseHCP / 18);
        int hcpIncMaxHole = courseHCP - (hcpAll * 18);
        log.debug("hcpAll " + hcpAll);
        log.debug("hcpIncMaxHole " + hcpIncMaxHole);

        // fill all holes with hcpAll value or initialize it with 0 if hcpAll is 0
        round.getScoreCard().forEach(scoreCard -> scoreCard.setHcp(hcpAll));

        List<Hole> holes = round.getCourse().getHoles();
        // get list of scorecard for player
        List<ScoreCard> playerScoreCard = round.getScoreCard().stream()
                .filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId())).collect(Collectors.toList());
        playerScoreCard.forEach(scoreCard -> {
            if (hcpIncMaxHole > 0 && holes.get(scoreCard.getHole() - 1).getSi() <= hcpIncMaxHole) {
                // if some holes needs hcp update increase them
                scoreCard.setHcp(hcpAll + 1);
            }

            // update STB net for each hole
            scoreCard.setStbNet(
                    holes.get(scoreCard.getHole() - 1).getPar() - (scoreCard.getStroke() - scoreCard.getHcp()) + 2);
            if (scoreCard.getStbNet() < 0) {
                scoreCard.setStbNet(0);
            }
            log.debug(scoreCard.getHole() + " " + scoreCard.getStbNet());
            // update STB gross for each hole
            scoreCard.setStbGross(holes.get(scoreCard.getHole() - 1).getPar() - scoreCard.getStroke() + 2);
            if (scoreCard.getStbGross() < 0) {
                scoreCard.setStbGross(0);
            }
        });

        retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbNet).sum());
        tournamentResult.setStbNet(tournamentResult.getStbNet() + retStb.get(0));

        retStb.add(playerScoreCard.stream().mapToInt(ScoreCard::getStbGross).sum());
        tournamentResult.setStbGross(tournamentResult.getStbGross() + retStb.get(1));

        return retStb;
    }

    @Transactional
    public List<Round> getAllPossibleRoundsForTournament(Long tournamentId) {

        var retRounds = new ArrayList<Round>();

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        var rounds =  roundService.findByDates(tournament.getStartDate(), tournament.getEndDate());

        if (!rounds.isEmpty()) {

            var tournamentPlayers = tournamentPlayerRepository.findByTournamentId(tournament.getId());
            var plrIdLst = tournamentPlayers.stream().map(TournamentPlayer::getPlayerId).collect(Collectors.toList());

            rounds.forEach(r -> {

                if (plrIdLst.containsAll(r.getPlayer().stream().map(Player::getId).collect(Collectors.toList()))) {
                    retRounds.add(r);
                } else {
                    log.info("The round with non matching player found: round id " + r.getId() + " with number of players: " + r.getPlayer().size());
                }
            });
        }

        return retRounds;
    }

    private int getCourseHCP(PlayerRound playerRound, Round round, Player player) {

        if (playerRound == null) {

            playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());
        }

        var courseTee = courseService.getTeeById(playerRound.getTeeId()).orElseThrow();

        // calculate course HCP
        int courseHCP = Math
                .round(playerRound.getWhs() * courseTee.getSr() / 113 + courseTee.getCr() - round.getCourse().getPar());

        log.debug("Course SR: " + courseTee.getSr());
        log.debug("Course CR: " + courseTee.getCr());
        log.debug("Course Par: " + round.getCourse().getPar());
        log.debug("Calculated course HCP: " + courseHCP);

        return courseHCP;
    }

    private TournamentResult buildEmptyTournamentResult(Player player) {

        var tournamentResult = new TournamentResult();
        tournamentResult.setPlayedRounds(1);
        tournamentResult.setPlayer(player);
        tournamentResult.setStbNet(0);
        tournamentResult.setStbGross(0);
        tournamentResult.setStrokeRounds(0);

        return tournamentResult;
    }

    // verifies if all scorecards have all 18 holes filled
    private void verifyRoundCorrectness(Round round) {

        round.getPlayer().forEach(player -> {

            // calculate played holes
            int playedHoles = (int) round.getScoreCard().stream()
                    .filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()) && scoreCard.getStroke() > 0).count();
            log.debug("Number of holes: " + playedHoles);
            if (playedHoles != TOURNAMENT_HOLES) {
                throw new TooFewHolesForTournamentException();
            }
        });
    }

    @Transactional
    public void closeTournament(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to close tournament result by unauthorized user");

        // set close flag
        tournament.setStatus(Tournament.STATUS_CLOSE);
        tournamentRepository.save(tournament);
    }

    @Transactional
    public void addPlayer(TournamentPlayer tournamentPlayer) throws DuplicatePlayerInTournamentException {

        var tournament = tournamentRepository.findById(tournamentPlayer.getTournamentId()).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to close tournament result by unauthorized user");

        //check if player exists
        var player = playerRepository.findById(tournamentPlayer.getPlayerId()).orElseThrow();

        // then check if tournament exists
        if (!tournamentRepository.existsById(tournamentPlayer.getTournamentId())) {
            throw new NoSuchElementException();
        }

        //prepare data to save
        tournamentPlayer.setNick(player.getNick());
        tournamentPlayer.setWhs(player.getWhs());

        // save entity
        // trow exception if player has been already added to the tournament
        try {
            tournamentPlayerRepository.save(tournamentPlayer);
        } catch (Exception ex) {
            throw new DuplicatePlayerInTournamentException();
        }
    }

    @Transactional
    public void deletePlayers(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament player result by unauthorized user");

        // remove only if tournaments does not have any results
        if (tournamentResultRepository.findByTournament(tournament).isEmpty()) {
            tournamentPlayerRepository.deleteByTournamentId(tournamentId);
        } else {
            throw new DeleteTournamentPlayerException();
        }
    }

    @Transactional
    public void deletePlayer(Long tournamentId, long playerId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament player result by unauthorized user");

        var player = new Player();
        player.setId(playerId);

        // remove only if tournaments does not have any results
        if (tournamentResultRepository.findByPlayerAndTournament(player, tournament).isEmpty()) {
            tournamentPlayerRepository.deleteByTournamentIdAndPlayerId(tournamentId, playerId);
        } else {
            throw new DeleteTournamentPlayerException();
        }
    }

    @Transactional
    public List<TournamentPlayer> getTournamentPlayers(Long tournamentId) {

        return tournamentPlayerRepository.findByTournamentId(tournamentId);

    }
}
