package optalp.chtplanning.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Time schedule for a session of patient
 */
@Data
@Accessors(fluent = true)
public class Allocation implements Comparable<Allocation> {
    /**
     * Per patient
     */
    private long id;
    private long sectorId;
    private int sessionDay;
    private Interval session = new Interval();
    private Interval consultation = new Interval();
    private Interval installation = new Interval();
    private int drugMixingDay;
    private Interval drugMixing = new Interval();
    private Interval treatment = new Interval();

    @Override
    public int compareTo(Allocation o) {
        return this.sessionDay - o.sessionDay;
    }
}
