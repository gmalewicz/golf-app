package com.greg.golf.service;

import com.greg.golf.controller.dto.EagleResultDto;
import com.greg.golf.entity.Cycle;
import com.greg.golf.entity.CycleResult;
import com.greg.golf.entity.CycleTournament;
import com.greg.golf.entity.helpers.Common;
import com.greg.golf.repository.CycleRepository;
import com.greg.golf.repository.CycleResultRepository;
import com.greg.golf.repository.CycleTournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@RequiredArgsConstructor
@Service("cycleService")
public class CycleService {

    private static final int ROUNDS_PER_TOURNAMENT = 4;

    private final CycleRepository cycleRepository;

    private final CycleTournamentRepository cycleTournamentRepository;

    private final CycleResultRepository cycleResultRepository;

    @Transactional
    public Cycle addCycle(Cycle cycle) {

        return cycleRepository.save(cycle);
    }

    @Transactional
    public CycleTournament addCycleTournament(CycleTournament cycleTournament, EagleResultDto[] eagleResultDto) {

        // filter to exclude players with too high handicap
        eagleResultDto = Arrays.stream(eagleResultDto)
                .filter(e -> e.getWhs() <= cycleTournament.getCycle().getMaxWhs())
                .toArray(EagleResultDto[]::new);

        // update to get the best round if required
        // the best round from the tournament will be saved at index 0 from four possible tournaments
        // the rest of tournaments will be removed
        if (Boolean.TRUE.equals(cycleTournament.getBestOf())) {
            for (var e : eagleResultDto) {
                int max = Arrays.stream(e.getR()).max().orElse(0);
                e.setR(new int[] {max, 0, 0, 0});
            }
        }

        addResults(cycleTournament, eagleResultDto);

        return cycleTournamentRepository.save(cycleTournament);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Modifying
    public void removeLastCycleTournament(Cycle cycle) {

        var tournaments = cycleTournamentRepository.findByCycleOrderById(cycle);

        if (!tournaments.isEmpty()) {
            // find the last tournament
            var lstTournament = tournaments.getLast();

            // update results for cycle with more than 1 tournament otherwise delete all results
            if (tournaments.size() > 1) {
                cycleResultRepository.saveAll(removeTournamentFromResult(cycle, lstTournament));
            } else {
                cycleResultRepository.deleteForCycle(cycle.getId());
            }
            // remove the last tournament
            cycleTournamentRepository.delete(lstTournament);
        }
    }

    private List<CycleResult> removeTournamentFromResult(Cycle cycle, CycleTournament cycleTournament) {

        var results = cycleResultRepository.findByCycle(cycle);


        results.forEach(result -> {
            // first remove results
            result.setResults(Arrays.stream(result.getResults())
                .limit(result.getResults().length - (long)ROUNDS_PER_TOURNAMENT).toArray());
            result.setHcp(Arrays.copyOf(result.getHcp(), result.getHcp().length - 1));
        });

        // second remove hcp
        results.forEach(result -> result.setHcp(Arrays.copyOf(result.getHcp(), result.getHcp().length - 1)));

        // then update totals
        return updCycleResultAndTotal(cycleTournament, results);

    }

    private void addResults(CycleTournament cycleTournament, EagleResultDto[] eagleResultDto) {

        // first verify if there are already results
        var cycleResults = cycleResultRepository.findByCycle(cycleTournament.getCycle());

        // if not process initial insert, if yes process update
        if (cycleResults.isEmpty()) {
            cycleResults = prepareTournament(cycleTournament, eagleResultDto);

        } else {
            List<CycleResult> newTournament = prepareTournament(cycleTournament, eagleResultDto);
            cycleResults = addTournamentToCycleResult( cycleResults,  newTournament);

        }

        // update total and cycle result
        cycleResultRepository.saveAll(updCycleResultAndTotal(cycleTournament, cycleResults));
    }

    private List<CycleResult> addTournamentToCycleResult(List<CycleResult> cycleResults, List<CycleResult> tournamentResults) {

        var finalCycleResults = new ArrayList<CycleResult>();

        //split data by series
        var seriesMap = tournamentResults.stream().collect(groupingBy(CycleResult::getSeries));

        //do it for each series
        seriesMap.forEach((series, tournamentResultsForSeries) -> {

            //select cycle results for proper series
            var cycleResultsForSeries = cycleResults.stream()
                    .filter(cycleResult -> cycleResult.getSeries().equals(series)).toList();

            // initialize result size
            var cycleResultSize = cycleResultsForSeries.getFirst().getResults().length;

            //create map from cycle result where key is player id
            var cycleResultMap = cycleResultsForSeries.stream()
                    .collect(Collectors.toMap(CycleResult::getPlayerName, cycleResult -> cycleResult));

            // update cycle results
            tournamentResultsForSeries.forEach(tournamentResult -> {

                //if player already exists
                if (cycleResultMap.containsKey(tournamentResult.getPlayerName())) {

                    var cycleResult = cycleResultMap.get(tournamentResult.getPlayerName());

                    cycleResult.setResults(IntStream.concat(Arrays.stream(cycleResult.getResults()),
                            Arrays.stream(tournamentResult.getResults())).toArray());

                    var arrList = new ArrayList<>( Arrays.asList(cycleResult.getHcp()));
                    arrList.add(tournamentResult.getHcp()[0]);
                    cycleResult.setHcp(arrList.toArray(cycleResult.getHcp()));

                    // if that player played the first time in the tournament
                } else {

                    tournamentResult.setResults(IntStream.concat(Arrays.stream(new int[cycleResultSize]),
                            Arrays.stream(tournamentResult.getResults())).toArray());

                    var hcpArray = new String[cycleResultSize/ROUNDS_PER_TOURNAMENT + 1];
                    Arrays.fill(hcpArray, "");
                    hcpArray[hcpArray.length - 1] = tournamentResult.getHcp()[0];
                    tournamentResult.setHcp(hcpArray);

                    cycleResultMap.put(tournamentResult.getPlayerName(), tournamentResult);

                }
            });

            // update players who did not play that tournament
            cycleResultMap.values().forEach(cycleResult -> {
                if (cycleResult.getResults().length == cycleResultSize) {
                    cycleResult.setResults(IntStream.concat(Arrays.stream(cycleResult.getResults()),
                            Arrays.stream(new int[ROUNDS_PER_TOURNAMENT])).toArray());

                    var arrList = new ArrayList<>( Arrays.asList(cycleResult.getHcp()));
                    arrList.add("");
                    cycleResult.setHcp(arrList.toArray(cycleResult.getHcp()));

                }
            });

            finalCycleResults.addAll(cycleResultMap.values());
        });

        return finalCycleResults;
    }


    private List<CycleResult> prepareTournament(CycleTournament cycleTournament, EagleResultDto[] eagleResultDto) {
        List<CycleResult> cycleTournamentResults =  Arrays.stream(eagleResultDto)
                .map(e -> {
                    var cycleResult = new CycleResult();
                    cycleResult.setCycle(cycleTournament.getCycle());
                    cycleResult.setPlayerName(e.getLastName() + " " + e.getFirstName());
                    cycleResult.setResults(e.getR());
                    cycleResult.setHcp(new String[] {e.getWhs().toString()});
                    cycleResult.setSeries(e.getSeries());
                    return cycleResult;})
                .toList();

        log.debug("initial cycle results created");

        return cycleTournamentResults;
    }

    private List<CycleResult> updCycleResultAndTotal(CycleTournament cycleTournament, List<CycleResult> cycleResults ) {

        // if all rounds should be included cycle results equals total
        if (cycleTournament.getCycle().getBestRounds() == 0) {
            cycleResults.forEach( cycleResult ->  {
                cycleResult.setCycleScore(
                        Arrays.stream(cycleResult.getResults())
                                .reduce(0, Integer::sum)
                );
                cycleResult.setTotal(cycleResult.getCycleScore());
            });
        // otherwise, get best rounds from cycle results according to specified rule
        } else {

            cycleResults.forEach( cycleResult -> {

                if (cycleTournament.getCycle().getSeries() == Common.CYCLE_SERIES_STB) {

                    cycleResult.setCycleScore(
                            Arrays.stream(cycleResult.getResults())
                                    .boxed()
                                    .sorted(Comparator.reverseOrder())
                                    .limit(cycleTournament.getCycle().getBestRounds())
                                    .reduce(0, Integer::sum)
                    );
                } else {
                    cycleResult.setCycleScore(
                            Arrays.stream(cycleResult.getResults())
                                    .boxed()
                                    .filter(result -> result > 0)
                                    .sorted(Comparator.naturalOrder())
                                    .limit(cycleTournament.getCycle().getBestRounds())
                                    .reduce(0, Integer::sum)
                    );
                }
                cycleResult.setTotal(
                        Arrays.stream(cycleResult.getResults())
                                .reduce(0, Integer::sum)
                );
            });
        }
        return cycleResults;
    }


    @Transactional(readOnly = true)
    public List<Cycle> findAllCycles() {
        return cycleRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public List<CycleTournament> findAllCycleTournaments(Long cycleId) {
        var cycle = new Cycle();
        cycle.setId(cycleId);
        return cycleTournamentRepository.findByCycleOrderById(cycle);
    }

    @Transactional(readOnly = true)
    public List<CycleResult> findCycleResults(Long cycleId) {
        var cycle = new Cycle();
        cycle.setId(cycleId);
        return cycleResultRepository.findByCycleOrderBySeriesAscCycleScoreDesc(cycle);
    }

    public void closeCycle(Long cycleId) {

        var cycle = cycleRepository.findById(cycleId).orElseThrow();

        // set close flag
        cycle.setStatus(Cycle.STATUS_CLOSE);
        cycleRepository.save(cycle);

    }

    @Transactional
    public void deleteCycle(Long cycleId) {

        cycleResultRepository.deleteForCycle(cycleId);
        cycleRepository.deleteById(cycleId);
    }


}
