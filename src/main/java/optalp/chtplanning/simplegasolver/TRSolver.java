package optalp.chtplanning.simplegasolver;

import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;
import optalp.chtplanning.common.solution.Solution;
import optalp.chtplanning.simplelptsolver.FFS_SumC_Solver;
import optalp.chtplanning.simplelptsolver.LPT_FFS_SumC_Solver;

import java.util.*;
import java.util.stream.Stream;

/**
 * Totally random genetic algorithm
 */
public class TRSolver extends LPT_FFS_SumC_Solver {
    private final int SOLUTION_POOL_SIZE;
    private final Random RANDOMIZER;

    public TRSolver(int solution_pool_size,
                    long seed) {
        SOLUTION_POOL_SIZE = solution_pool_size;
        RANDOMIZER = new Random(seed);
    }

    @Override
    public MinimizingMeanFlowTimeSolution solve(Param param,
                                                List<PatientCycleDemand> cycleDemands,
                                                List<Allocation> existingAllocations)
            throws SolverException {
        Stream<List<PatientCycleDemand>> shuffledCycleDemands =
                Stream.generate(() -> {
                    List<PatientCycleDemand> _cycleDemands =
                            new ArrayList<>(cycleDemands);
                    Collections.shuffle(_cycleDemands, RANDOMIZER);
                    return _cycleDemands;
                });
        // Iterate
        return shuffledCycleDemands.limit(SOLUTION_POOL_SIZE).parallel()
                .map(shuffledDemands -> {
                    FFS_SumC_Solver fifoSolver = new FFS_SumC_Solver();
                    try {
                        return fifoSolver.solve(param,
                                shuffledDemands,
                                existingAllocations);
                    } catch (SolverException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(Solution::getObjective))
                .orElseThrow(() ->
                        new SolverException("Unsolvable"));
    }
}
