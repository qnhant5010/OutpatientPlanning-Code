import lombok.Value;
import optalp.chtplanning.common.objective.Objective;

@Value
class Record {
    String solverName;
    int problemSize;
    String scenario;
    int testIndex;
    Objective objective;
    /**
     * In milliseconds
     */
    Long time;
}
