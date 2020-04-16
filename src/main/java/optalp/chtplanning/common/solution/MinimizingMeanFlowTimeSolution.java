package optalp.chtplanning.common.solution;

import optalp.chtplanning.common.objective.IntegerObjective;

public class MinimizingMeanFlowTimeSolution extends Solution<IntegerObjective> {

    private final int timeSlotsPerDay;

    public MinimizingMeanFlowTimeSolution(int timeSlotsPerDay) {
        this.timeSlotsPerDay = timeSlotsPerDay;
    }

    @Override
    public void deduceObjectiveValue() {
        objective = new IntegerObjective(
                plannings.keySet().stream()
                        .mapToInt(demandId -> plannings.get(demandId)
                                .stream()
                                .mapToInt(allocation -> allocation.sessionDay() >= 1 ?
                                                        (allocation.sessionDay() - 1) * timeSlotsPerDay + allocation.treatment().end()
                                                                                     : 0
                                         )
                                .sum())
                        .sum());
    }
}
