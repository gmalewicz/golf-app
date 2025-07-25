package com.greg.golf.service;

import java.util.*;
import java.util.stream.Collectors;

import com.greg.golf.configurationproperties.TournamentServiceConfig;
import com.greg.golf.entity.*;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.error.*;
import com.greg.golf.repository.*;
import com.greg.golf.service.helpers.RoleVerification;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.greg.golf.service.events.RoundEvent;

import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service("tournamentService")
@RequiredArgsConstructor
public class TournamentService {

    private static final int TOURNAMENT_HOLES = 18;

    private final TournamentServiceConfig tournamentServiceConfig;

    private final TournamentResultRepository tournamentResultRepository;
    private final TournamentRepository tournamentRepository;
    private final RoundService roundService;
    private final CourseService courseService;
    private final PlayerRoundRepository playerRoundRepository;
    private final TournamentRoundRepository tournamentRoundRepository;
    private final PlayerRepository playerRepository;
    private final TournamentPlayerRepository tournamentPlayerRepository;
    private final TournamentNotificationRepository tournamentNotificationRepository;
    private final EmailServiceImpl emailServiceImpl;
    private final TemplateEngine templateEngine;
    private final PlayerService playerService;


    public static final int DEFAULT_PLAYING_MULTIPLIER = 1;
    public static final int SORT_STB_NET = 1;
    public static final int SORT_STB = 2;
    public static final int SORT_STR_NET = 3;
    public static final int SORT_STR = 4;

    @Lazy
    private final TournamentService self;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public void deleteTournament(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // then verify if player is allowed to delete result
        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tournament result by unauthorized user");

        // remove notifications
        tournamentNotificationRepository.deleteByTournamentId(tournamentId);

        tournament
            .getTournamentResult()
            .stream()
            .map(TournamentResult::getId)
            .toList()
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

        // then clear tournament flag for player round
        playerRoundRepository.clearTournamentForPlayer(tournamentResult.getPlayer().getId(), tournamentResult.getTournament().getId());

        // then delete result
        var rstLst = tournamentResult.getTournament().getTournamentResult()
                                            .stream()
                                            .filter(rst -> rst.getId().equals(resultId))
                                            .toList();
        tournamentResult.getTournament().getTournamentResult().removeAll(rstLst);
        tournamentRepository.save(tournamentResult.getTournament());
    }

    @Transactional(readOnly = true)
    public List<Tournament> findAllTournamentsPageable(Integer pageNo) {
        return tournamentRepository.findAllByOrderByIdDesc(PageRequest.of(pageNo, tournamentServiceConfig.getPageSize()));
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

        var gc = new GregorianCalendar();
        round.setRoundDate(gc.getTime());

        round = roundService.saveRound(round);
        entityManager.detach(round);

        var tournamentLst = self.addRound(tournamentId, round.getId(), true);

        //it must be one and only one result
        if (tournamentLst == null || tournamentLst.size() != 1) {
            return null;
        }

        return tournamentLst.getFirst();
    }

    @Transactional
    public List<TournamentRound> addRound(Long tournamentId, Long roundId, boolean updateResults) {

        // first find the round in database
        var round = roundService.getWithPlayers(roundId).orElseThrow();

        // get tournament object
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        tournament = tournamentRepository.findById(tournament.getId()).orElseThrow();

        // update tournament result
        if (updateResults) {
            return self.updateTournamentResult(round, tournament);
        }
        return new ArrayList<>();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @EventListener
    public void handleRoundEvent(RoundEvent ignoredRoundEvent) {
        log.info("Handling round event... however tournament cannot be updated that way");
    }

    @Transactional
    public List<TournamentRound> updateTournamentResult(Round round, Tournament tournament) {

       var tournamentRoundLst = new ArrayList<TournamentRound>();

       var tournamentPlayers = tournamentPlayerRepository
                                .findByTournamentId(tournament.getId())
                                .stream()
                                .collect(Collectors.toMap(TournamentPlayer::getPlayerId, TournamentPlayer::getWhs));

        // first verify if round has 18 holes played for each player
        verifyRoundCorrectness(round);

        // iterate through round players and check if they already added
        round.getPlayer().forEach(player -> {

            var playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());

            if (playerRound.getTournamentId() == null && tournamentPlayers.containsKey(playerRound.getPlayerId())) {

                Optional<TournamentResult> tournamentResultOpt = tournamentResultRepository
                        .findByPlayerAndTournament(player, tournament);
                tournamentResultOpt.ifPresentOrElse(tournamentResult -> {
                    log.debug("Attempting to update tournament result");

                    tournamentResult.setPlayedRounds(tournamentResult.getPlayedRounds() + 1);
                    int grossStrokes = 0;
                    int netStrokes = 0;

                    // calculate course HCP
                    int courseHCP = getCourseHCP(playerRound, round, player, tournamentPlayers.get(player.getId()));
                    int playingHCP = getPlayingHcp(tournament, courseHCP);

                    List<Integer> stb = updateSTB(tournamentResult, round,  player, playingHCP);
                    // check if round is applicable for stroke statistic
                    boolean strokeApplicable = applicableForStroke(round, player);
                    if (strokeApplicable) {
                        tournamentResult.increaseStrokeRounds();
                        grossStrokes = getGrossStrokes(player, round);
                        netStrokes = getNetStrokes(grossStrokes, playingHCP);
                    }
                    tournamentResult.setStrokesBrutto(tournamentResult.getStrokesBrutto() + grossStrokes);
                    tournamentResult.setStrokesNetto(tournamentResult.getStrokesNetto() + netStrokes);

                    // save entity
                    tournamentResultRepository.save(tournamentResult);

                    tournamentRoundLst.add(addTournamentRound(stb.get(1), stb.get(0), grossStrokes, netStrokes,
                            getScoreDifferential(playerRound, round, player), round.getCourse().getName(),
                            tournamentResult, strokeApplicable, round.getId(), playingHCP,
                            tournamentPlayers.get(player.getId()), courseHCP));

                    // here needs to be an update of TournamentResults in case if number of added rounds is greater
                    // than bestRounds assuming that bestRounds is not 0
                    updateForBestRounds(tournament, tournamentResult);


                }, () -> {
                    log.debug("Attempting to add the new round to tournament result");
                    // if it is the first record to be added to result than create it
                    var tournamentResult = buildEmptyTournamentResult(player);
                    tournamentResult.setTournament(tournament);

                    // calculate course HCP
                    int courseHCP = getCourseHCP(playerRound, round, player, tournamentPlayers.get(player.getId()));
                    int playingHCP = getPlayingHcp(tournament, courseHCP);

                    // update stb result
                    List<Integer> stb = updateSTB(tournamentResult, round,  player, playingHCP);
                    // check if round is applicable for stroke statistic
                    boolean strokeApplicable = applicableForStroke(round, player);
                    if (strokeApplicable) {
                        tournamentResult.increaseStrokeRounds();
                        // get gross and net strokes
                        tournamentResult.setStrokesBrutto(getGrossStrokes(player, round));
                        tournamentResult.setStrokesNetto(
                                getNetStrokes(tournamentResult.getStrokesBrutto(), playingHCP));
                    } else {
                        tournamentResult.setStrokesBrutto(0);
                        tournamentResult.setStrokesNetto(0);
                    }
                    // save entity
                    tournamentResultRepository.save(tournamentResult);

                    tournamentRoundLst.add(addTournamentRound(stb.get(1), stb.get(0), tournamentResult.getStrokesBrutto(),
                            tournamentResult.getStrokesNetto(), getScoreDifferential(playerRound, round, player),
                            round.getCourse().getName(), tournamentResult, strokeApplicable, round.getId(), playingHCP,
                            tournamentPlayers.get(player.getId()), courseHCP));

                });

                // set tournament id in player_round
                playerRound.setTournamentId(tournament.getId());
                playerRoundRepository.save(playerRound);
            }
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
                                              long roundId, int playingHCP, float hcp, int courseHcp) {

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
        tournamentRound.setPlayingHcp(playingHCP);
        tournamentRound.setHcp(hcp);
        tournamentRound.setCourseHcp(courseHcp);

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

        return (113 / (float) playerRound.getSr()) * (self.getCorrectedStrokes(player, round) - playerRound.getCr());

    }

    // calculate gross strokes
    @Transactional
    public int getGrossStrokes(Player player, Round round) {

        printDebugInfo(player, round);

        // calculate gross result
        int grossStrokes = round.getScoreCard().stream().filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()))
                .mapToInt(ScoreCard::getStroke).sum();
        log.debug("Calculated gross strokes: {}", grossStrokes);
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

        printDebugInfo( player, round);

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

        log.debug("Calculated corrected strokes: {}", grossStrokes);
        return grossStrokes;
    }

    // calculate net strokes
    @Transactional
    public int getNetStrokes(int grossStrokes, int playingHCP) {

        int netStrokes = grossStrokes - playingHCP;
        if (netStrokes < 0) {
            netStrokes = 0;
        }

        log.debug("Calculated net strokes: {}", netStrokes);
        return netStrokes;
    }

    // returns STB net at index 0 and STB gross at index 1
    @Transactional
    public List<Integer> updateSTB(TournamentResult tournamentResult, Round round, Player player, int playingHCP) {

        // create List of ret values
        List<Integer> retStb = new ArrayList<>();

        // calculate hole HCP for player
        int hcpAll = (int) Math.floor((double) playingHCP / 18);
        int hcpIncMaxHole = playingHCP - (hcpAll * 18);
        log.debug("hcpAll {}", hcpAll);
        log.debug("hcpIncMaxHole {}", hcpIncMaxHole);

        // fill all holes with hcpAll value or initialize it with 0 if hcpAll is 0
        round.getScoreCard().forEach(scoreCard -> scoreCard.setHcp(hcpAll));

        List<Hole> holes = round.getCourse().getHoles();
        // get list of scorecard for player
        List<ScoreCard> playerScoreCard = round.getScoreCard()
                                            .stream()
                                            .filter(scoreCard -> scoreCard.getPlayer().getId().equals(player.getId()))
                                            .toList();
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
            log.debug("{} {}", scoreCard.getHole(), scoreCard.getStbNet());
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
            var plrIdLst = tournamentPlayers
                            .stream()
                            .map(TournamentPlayer::getPlayerId)
                            .toList();

            rounds.forEach(r -> {

                if (Collections.disjoint(plrIdLst,
                                         playerRoundRepository.findByRoundIdOrderByPlayerId(r.getId())
                                                .orElseThrow()
                                                .stream()
                                                .filter(pr -> pr.getTournamentId() == null)
                                                .map(PlayerRound::getPlayerId)
                                                .toList())) {

                    log.info("The round with non matching player found: round id {} with number of players: {}", r.getId(), r.getPlayer().size());

                } else {

                    retRounds.add(r);
                }
            });
        }

        return retRounds;
    }

    // use multiplier to calculate playing HCP
    // put cap on playing hcp if required
    private int getPlayingHcp(Tournament tournament, int courseHcp) {

        int retPlayingHcp = courseHcp;

        // first apply multiplier
        if (tournament.getPlayHcpMultiplayer() != DEFAULT_PLAYING_MULTIPLIER) {
            retPlayingHcp = Math.round(retPlayingHcp * tournament.getPlayHcpMultiplayer());
        }

        // then apply cap
        if (retPlayingHcp > tournament.getMaxPlayHcp()) {
            retPlayingHcp = tournament.getMaxPlayHcp();
        }

        return retPlayingHcp;
    }

    private int getCourseHCP(PlayerRound playerRound, Round round, Player player, Float playerHcp) {

        if (playerRound == null) {

            playerRound = roundService.getForPlayerRoundDetails(player.getId(), round.getId());
        }

        var courseTee = courseService.getTeeById(playerRound.getTeeId()).orElseThrow();

        // calculate course HCP
        int courseHCP = Math
                .round(playerHcp * courseTee.getSr() / 113 + courseTee.getCr() - round.getCourse().getPar());

        log.debug("Course SR: {}", courseTee.getSr());
        log.debug("Course CR: {}", courseTee.getCr());
        log.debug("Course Par: {}", round.getCourse().getPar());
        log.debug("Calculated course HCP: {}", courseHCP);

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
            log.debug("Number of holes: {}", playedHoles);
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

        // remove notifications
        tournamentNotificationRepository.deleteByTournamentId(tournamentId);
    }

    @Transactional
    public void addPlayer(TournamentPlayer tournamentPlayer) throws DuplicatePlayerInTournamentException {

        var tournament = tournamentRepository.findById(tournamentPlayer.getTournamentId()).orElseThrow();
        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to add player by unauthorized user");

        //check if player exists
        var player = playerRepository.findById(tournamentPlayer.getPlayerId()).orElseThrow();

        //prepare data to save
        tournamentPlayer.setNick(player.getNick());
        tournamentPlayer.setWhs(player.getWhs());
        tournamentPlayer.setSex(player.getSex());

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

    @Transactional
    public void updatePlayerHcp(Long tournamentId, Long playerId, Float whs) throws HcpChangeNotAllowedException {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to update player handicap by unauthorized user");

        var tournamentPlayer = tournamentPlayerRepository.findByTournamentIdAndPlayerId(tournamentId, playerId).orElseThrow();

        // verify if tournament allows for hcp modification
        if ((!tournament.getCanUpdateHcp() &&
                tournamentResultRepository.findByPlayerAndTournament(tournament.getPlayer(), tournament).isPresent()) ||
                tournament.getStatus() == Tournament.STATUS_CLOSE
            ) {
            throw new HcpChangeNotAllowedException();
        }

        tournamentPlayer.setWhs(whs);
        tournamentPlayerRepository.save(tournamentPlayer);
    }

    @Transactional
    public void addTeeTimes(Long tournamentId, TeeTimeParameters teeTimeParameters) {

        if  (teeTimeParameters.getTeeTimes().isEmpty()) {
            return;
        }
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to add tee times by unauthorized user");

        // delete existing tee times if any and replace it
        // updates are not supported
        teeTimeParameters.setTournament(tournament);
        teeTimeParameters.getTeeTimes().forEach(teeTime -> teeTime.setTeeTimeParameters(teeTimeParameters));
        tournament.setTeeTimeParameters(teeTimeParameters);
        tournamentRepository.save(tournament);
    }

    @Transactional
    public TeeTimeParameters getTeeTimes(Long tournamentId) {

        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        return tournament.getTeeTimeParameters();

    }

    @Transactional
    public void deleteTeeTimes(Long tournamentId) {
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();

        // only tournament owner can do it
        RoleVerification.verifyPlayer(tournament.getPlayer().getId(), "Attempt to delete tee times by unauthorized user");

        tournament.setTeeTimeParameters(null);
        tournamentRepository.save(tournament);

    }

    @Transactional(readOnly = true)
    public int processNotifications(Long tournamentId, Integer sort) throws GeneralException {

        int sentNotifications = 0;

        var notifications = tournamentNotificationRepository.findByTournamentId(tournamentId);

        if (!notifications.isEmpty()) {

            var recipients = new ArrayList<String>();

            notifications.forEach(notification -> {

                var email = playerService.getEmail(notification.getPlayerId());

                if (email != null) {
                    recipients.add(email);
                }
            });

            if (!recipients.isEmpty()) {

                var tournament = tournamentRepository.findById(tournamentId);

                // only tournament owner can do it
                RoleVerification.verifyPlayer(tournament.orElseThrow().getPlayer().getId(), "Attempt to process notifications by unauthorized user");

                List<TournamentResult> sortedResults = sortResults(tournament.orElseThrow(), sort);

                var context = new Context();
                context.setVariable("results", sortedResults);
                context.setVariable("tournamentName", tournament.orElseThrow().getName());

                String body = templateEngine.process("TournamentResultsTemplate.html", context);

                try {
                    log.info("Number of notifications for sending: {}", recipients.size());
                    emailServiceImpl.sendEmail(recipients.toArray(new String[0]), "Tournament results updated - " + tournament.orElseThrow().getName(), body);
                    sentNotifications = recipients.size();
                } catch (MessagingException e) {
                    throw new GeneralException();
                }
            }
        }
        return sentNotifications;
    }

    private List<TournamentResult> sortResults(Tournament tournament, Integer sort) {

        List<TournamentResult> sortedResults = switch (sort) {
            case SORT_STB_NET -> tournament.getTournamentResult()
                    .stream()
                    .sorted(Comparator.comparingInt(TournamentResult::getStbNet).reversed())
                    .collect(Collectors.toList());
            case SORT_STB -> tournament.getTournamentResult()
                    .stream()
                    .sorted(Comparator.comparingInt(TournamentResult::getStbGross).reversed())
                    .collect(Collectors.toList());
            case SORT_STR -> tournament.getTournamentResult()
                    .stream()
                    .sorted(Comparator.comparingInt(TournamentResult::getStrokesBrutto))
                    .collect(Collectors.toList());
            case SORT_STR_NET -> tournament.getTournamentResult()
                    .stream()
                    .sorted(Comparator.comparingInt(TournamentResult::getStrokesNetto))
                    .collect(Collectors.toList());
            default -> new ArrayList<>();
        };

        if (tournament.getBestRounds() == Common.ALL_ROUNDS && (sort == SORT_STR || sort == SORT_STR_NET)) {
            sortedResults = sortedResults
                    .stream()
                    .sorted(Comparator.comparingInt(TournamentResult::getStrokeRounds).reversed())
                    .collect(Collectors.toList());
        } else if (tournament.getBestRounds() != Common.ALL_ROUNDS && (sort == SORT_STR || sort == SORT_STR_NET)) {
            var resultsPlayedBestRounds  = sortedResults
                    .stream()
                    .filter(sr -> sr.getPlayedRounds() >= tournament.getBestRounds())
                    .toList();
            var resultsNotPlayedBestRounds = sortedResults
                    .stream()
                    .filter(sr -> sr.getPlayedRounds() < tournament.getBestRounds())
                    .sorted(Comparator.comparingInt(TournamentResult::getStrokeRounds).reversed())
                    .toList();
            sortedResults = new ArrayList<>();
            sortedResults.addAll(resultsPlayedBestRounds);
            sortedResults.addAll(resultsNotPlayedBestRounds);
        }

        return sortedResults;
    }

    @Transactional
    public void addNotification(Long tournamentId) throws DuplicateNotificationException, MailNotSetException {

        Long playerId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info("trying to add notifications for tournament: {} and player: {}", tournamentId, playerId);

        // check if player has defined email
        var player = playerRepository.findById(playerId);
        if (player.orElseThrow().getEmail() == null) {
            throw new MailNotSetException();
        }

        // check if tournament is not closed
        var tournament = tournamentRepository.findById(tournamentId).orElseThrow();
        if (tournament.getStatus() == Tournament.STATUS_OPEN) {

            var tournamentNotification = new TournamentNotification();
            tournamentNotification.setTournamentId(tournamentId);
            tournamentNotification.setPlayerId(playerId);

            try {
                tournamentNotificationRepository.save(tournamentNotification);
            } catch (Exception ex) {
                throw  new DuplicateNotificationException();
            }

        }
    }

    @Transactional
    public void removeNotification(Long tournamentId) {

        Long playerId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        log.info("trying to remove notifications for tournament: {} and player: {}", tournamentId, playerId);

        tournamentNotificationRepository.deleteByTournamentIdAndPlayerId(tournamentId, playerId);
    }

    private void printDebugInfo(Player player, Round round) {
        log.debug("player: {}", player);
        log.debug("round: {}", round);
    }

}
