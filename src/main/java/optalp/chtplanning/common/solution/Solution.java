package optalp.chtplanning.common.solution;

import optalp.chtplanning.common.Allocation;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.objective.Objective;

import java.util.*;

/**
 * For each planning register, a solution should calculate the corresponding
 * objective value
 *
 * @param <O> type of objective value
 */
public abstract class Solution<O extends Objective> {
    /**
     * Regroup {@link Allocation} per {@link PatientCycleDemand}'s id
     */
    Map<Long, TreeSet<Allocation>> plannings = new HashMap<>();
    O objective;

    public void save(Collection<Allocation> allocations, Long demandId) {
        plannings.put(demandId, new TreeSet<>(allocations));
    }

    public Map<Long, TreeSet<Allocation>> viewPlannings() {
        return new HashMap<>(plannings);
    }

    /**
     *
     */
    public abstract void deduceObjectiveValue();

    public O getObjective() {
        return objective;
    }
}
