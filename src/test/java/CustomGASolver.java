import io.jenetics.Alterer;
import io.jenetics.EnumGene;
import io.jenetics.Optimize;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import optalp.chtplanning.common.Allocation;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.SolverException;
import optalp.chtplanning.common.solution.MinimizingMeanFlowTimeSolution;
import optalp.chtplanning.simplegasolver.GASolver;
import optalp.chtplanning.simplelptsolver.FFS_SumC_Solver;

import java.util.ArrayList;
import java.util.List;

public class CustomGASolver extends GASolver {
    private int populationSize;
    private int maxGen;
    private Alterer<EnumGene<PatientCycleDemand>, Integer> alterer;
    private List<Integer> bestObjectives;

    CustomGASolver(int populationSize,
                          int maxGen,
                          Alterer<EnumGene<PatientCycleDemand>, Integer> alterer) {
        this.populationSize = populationSize;
        this.maxGen = maxGen;
        this.bestObjectives = new ArrayList<>(maxGen);
        this.alterer = alterer;
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
                .optimize(Optimize.MINIMUM)
                .populationSize(populationSize)
                .alterers(alterer)
                .build();
        List<PatientCycleDemand> bestChromosome;
        bestChromosome = CODEC.decode(
                engine.stream()
                        .limit(maxGen)
                        .peek(evolutionResult -> bestObjectives.add(evolutionResult.getBestFitness()))
                        .collect(EvolutionResult.toBestGenotype()))
                .asList();
        // Compile to solution
        return new FFS_SumC_Solver().solve(param,
                                           bestChromosome,
                                           existingAllocations);
    }

    public List<Integer> getBestObjectivesEvolution() {
        return bestObjectives;
    }
}
