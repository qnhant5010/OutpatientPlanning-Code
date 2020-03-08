package optalp.chtplanning.simplegasolver;

import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;
import optalp.chtplanning.simplelptsolver.FFS_SumC_Solver;

import java.util.List;

/**
 * Genetic algorithm with few population and generations
 */
public class GASolver extends Solver<MinimizingMeanFlowTimeSolution> {
    protected List<Allocation> existingAllocations;
    private int POPULATION_SIZE;
    private int MAX_GEN;
    private int STEADY_GEN;

    public GASolver(int populationSize, int maxGen, int steadyGen) {
        POPULATION_SIZE = populationSize;
        MAX_GEN = maxGen;
        STEADY_GEN = steadyGen;
        if (steadyGen <= 0) throw new IllegalArgumentException("Steady gen " +
                "must be positive non null");
    }

    protected GASolver() {}

    @Override
    public MinimizingMeanFlowTimeSolution solve(Param param,
                                                List<PatientCycleDemand> cycleDemands,
                                                List<Allocation> existingAllocations)
            throws SolverException {
        this.param = param;
        this.existingAllocations = existingAllocations;
        final Codec<ISeq<PatientCycleDemand>, EnumGene<PatientCycleDemand>> CODEC
                = Codecs.ofPermutation(ISeq.of(cycleDemands));
        Engine<EnumGene<PatientCycleDemand>, Integer> engine = Engine
                .builder(this::eval, CODEC)
                .optimize(Optimize.MINIMUM)
                .populationSize(POPULATION_SIZE)
                .alterers(
                        new SwapMutator<>(),
                        new PartiallyMatchedCrossover<>(0.35))
                .build();
        List<PatientCycleDemand> bestChromosome;
        if (MAX_GEN > 0)
            bestChromosome = CODEC.decode(
                    engine.stream()
                            .limit(Limits.bySteadyFitness(STEADY_GEN))
                            .limit(MAX_GEN)
                            .collect(EvolutionResult.toBestGenotype()))
                    .asList();
        else
            bestChromosome = CODEC.decode(
                    engine.stream()
                            .limit(Limits.bySteadyFitness(STEADY_GEN))
                            .collect(EvolutionResult.toBestGenotype())
            ).asList();
        // Compile to solution
        FFS_SumC_Solver ffAllocator = new FFS_SumC_Solver();
        return ffAllocator.solve(
                param,
                bestChromosome,
                existingAllocations
        );
    }

    protected Integer eval(final ISeq<PatientCycleDemand> cycleDemands) {
        FFS_SumC_Solver ffAllocator = new FFS_SumC_Solver();
        try {
            return ffAllocator.solve(
                    param,
                    cycleDemands.asList(),
                    existingAllocations
            ).getObjective().getValue();
        } catch (SolverException e) {
            return Integer.MAX_VALUE;
        }
    }
}