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
    private int startSession;
    private int endSession;
    private int startConsultation;
    private int endConsultation;
    private int startInstallation;
    private int endInstallation;
    private int medPrepDay;
    private int startMedPrep;
    private int endMedPrep;
    private int startTreatment;
    private int endTreatment;

    @Override
    public int compareTo(Allocation o) {
        return this.sessionDay - o.sessionDay;
    }
}
