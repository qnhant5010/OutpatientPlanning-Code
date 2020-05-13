package optalp.chtplanning.simplegasolver;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import optalp.chtplanning.common.Allocation;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.SolverException;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;
import optalp.chtplanning.heuristicsolver.FF_SumC_Solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class CustomizableGASolver extends GASolver {
    private final int populationSize;
    private final int maxGen;
    private final Alterer<EnumGene<PatientCycleDemand>, Integer> alterer;
    private final List<Integer> bestObjectives;
    private final Executor executor;
    private final double offspringFraction;
    private final Selector<EnumGene<PatientCycleDemand>, Integer> selector;

    public CustomizableGASolver(int populationSize,
                                int maxGen,
                                Alterer<EnumGene<PatientCycleDemand>, Integer> alterer,
                                Executor executor,
                                double offspringFraction,
                                Selector<EnumGene<PatientCycleDemand>, Integer> selector) {
        this.populationSize = populationSize;
        this.maxGen = maxGen;
        this.bestObjectives = new ArrayList<>(maxGen);
        this.alterer = alterer;
        this.executor = executor;
        this.offspringFraction = offspringFraction;
        this.selector = selector;
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
                .offspringFraction(offspringFraction)
                .optimize(Optimize.MINIMUM)
                .populationSize(populationSize)
                .alterers(alterer)
                .maximalPhenotypeAge(Integer.MAX_VALUE)
                .executor(executor)
                .selector(selector)
                .build();
        List<PatientCycleDemand> bestChromosome;
        bestChromosome = CODEC.decode(
                engine.stream()
                        .limit(maxGen)
                        .peek(evolutionResult -> bestObjectives.add(evolutionResult.bestFitness()))
                        .collect(EvolutionResult.toBestGenotype()))
                .asList();
        // Compile to solution
        return new FF_SumC_Solver().solve(param,
                                          bestChromosome,
                                          existingAllocations);
    }

    public List<Integer> getBestObjectivesEvolution() {
        return bestObjectives;
    }
}
