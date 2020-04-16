package optalp.chtplanning.common.solution;

import optalp.chtplanning.common.Allocation;
import optalp.chtplanning.common.PatientCycleDemand;
import optalp.chtplanning.common.objective.Objective;

import java.util.*;
import java.util.function.Function;

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
    Map<Long, Set<Allocation>> plannings = new HashMap<>();
    O objective;

    /**
     * Save the proposed allocations into planning with {@link HashSet}
     *
     * @param allocations
     * @param demandId
     */
    public void save(Collection<Allocation> allocations, Long demandId) {
        save(allocations,
             demandId,
             HashSet::new);
    }

    /**
     * Save the proposed allocations into planning with defined setFactory
     *
     * @param allocations
     * @param demandId
     * @param setFactory
     */
    public void save(Collection<Allocation> allocations,
                     Long demandId,
                     Function<Collection<Allocation>, Set<Allocation>> setFactory) {
        plannings.put(demandId,
                      setFactory.apply(allocations));
    }

    /**
     * @return a copy of plannings
     */
    public Map<Long, Set<Allocation>> viewPlannings() {
        return new HashMap<>(plannings);
    }

    /**
     * Lazily evaluate objective value
     */
    public abstract void deduceObjectiveValue();

    public O getObjective() {
        return objective;
    }
}
