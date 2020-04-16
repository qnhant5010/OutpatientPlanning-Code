package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Time schedule for a session of patient
 */
@Data
@Accessors(fluent = true)
@Builder
public class Allocation implements Comparable<Allocation> {
    /**
     * Per patient
     */
    private long id;
    private long sectorId;
    private int sessionDay;
    private Interval session;
    private Interval consultation;
    private Interval installation;
    private int drugMixingDay;
    private Interval drugMixing;
    private Interval treatment;

    @Override
    public int compareTo(Allocation o) {
        return this.sessionDay - o.sessionDay;
    }
}
