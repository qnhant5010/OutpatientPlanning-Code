package optalp.chtplanning.simplelptsolver;

import optalp.chtplanning.common.*;
import optalp.chtplanning.common.solution.MinimizingMakespanSolution;

import java.util.Comparator;
import java.util.List;

public class LPTMinSumCmaxSolver extends FirstFitStrategyAllocator {

    @Override
    public MinimizingMakespanSolution solve(Param param,
                          List<PatientCycleDemand> cycleDemands,
                          List<Allocation> existingAllocations)
            throws SolverException {
        this.param = param;
        solution = new MinimizingMakespanSolution(param.getNumTimeSlots());
        initWorkloadWith(existingAllocations);
        // Sort by longest possible processing time
        cycleDemands.sort(Comparator.comparing(this::getTotalLength).reversed());
        cycleDemands.forEach(this::allocate);
        solution.deduceObjectiveValue();
        return solution;
    }

    private int getTotalLength(PatientCycleDemand cycleDemand) {
        return cycleDemand.getRdvDemands().stream().mapToInt(
                rdvDemand -> (rdvDemand.isNeedingConsultation() ?
                        param.getConsultationLength() : 0)
                        + param.getInstallationLength()
                        + rdvDemand.getMedPrepDuration()
                        + rdvDemand.getTreatmentDuration()
        ).sum();
    }

}
