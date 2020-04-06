package optalp.chtplanning.simplegasolver;

import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.SwapMutator;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;
import optalp.chtplanning.heuristicsolver.FF_SumC_Solver;

import java.util.List;
import java.util.concurrent.Executor;

public class SimpleGASolver extends Solver<MinimizingMeanFlowTimeSolution> {
    private List<Allocation> existingAllocations;
    private int POPULATION_SIZE;
    private long MAX_GEN;
    private Executor EXECUTOR;

    public SimpleGASolver(int populationSize,
                          int maxGen,
                          Executor executor) {
        POPULATION_SIZE = populationSize;
        MAX_GEN = maxGen;
        EXECUTOR = executor;
    }

    @Override
    public MinimizingMeanFlowTimeSolution solve(Param param,
                                                List<PatientCycleDemand> cycleDemands,
                                                List<Allocation> existingAllocations) throws SolverException {
        this.param = param;
        this.existingAllocations = existingAllocations;
        final Codec<ISeq<PatientCycleDemand>, EnumGene<PatientCycleDemand>> CODEC
                = Codecs.ofPermutation(ISeq.of(cycleDemands));
        Engine<EnumGene<PatientCycleDemand>, Integer> engine = Engine
                .builder(this::eval, CODEC)
                .executor(EXECUTOR)
                .optimize(Optimize.MINIMUM)
                .populationSize(POPULATION_SIZE)
                .offspringFraction(0.6)
                .alterers(
                        new SwapMutator<>(0.05),
                        new PartiallyMatchedCrossover<>(1.00))
                .build();
        // Compile to solution
        FF_SumC_Solver ffAllocator = new FF_SumC_Solver();
        return ffAllocator.solve(param,
                                 CODEC.decode(
                                         engine.stream()
                                               .limit(Limits.byFixedGeneration(MAX_GEN))
                                               .collect(EvolutionResult.toBestGenotype()))
                                      .asList(),
                                 existingAllocations);
    }

    protected Integer eval(final ISeq<PatientCycleDemand> cycleDemands) {
        try {
            return new FF_SumC_Solver().solve(param,
                                              cycleDemands.asList(),
                                              existingAllocations)
                                       .getObjective()
                                       .getValue();
        } catch (SolverException e) {
            return Integer.MAX_VALUE;
        }
    }
}
