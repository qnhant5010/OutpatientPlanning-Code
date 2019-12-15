package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Data;

/**
 * Per patient's demand
 */
@Data
@Builder
public class PatientRdvDemand {
    private int id;
    /**
     * 0 means no speciality required
     */
    private int sectorId;
    /**
     * In days
     */
    private int afterLastRequest;
    private boolean needingConsultation;
    private boolean medPreparedSameDay;
    /**
     * In minutes
     */
    private int medPrepDuration;
    /**
     * In minutes
     */
    private int treatmentDuration;
}
