package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Value;

/**
 * Per patient's demand
 */
@Value
@Builder
public class PatientRdvDemand {
    int id;
    /**
     * 0 means no speciality required
     */
    int sectorId;
    /**
     * In days
     */
    int afterLastRequest;
    boolean needingConsultation;
    boolean medPreparedSameDay;
    /**
     * In minutes
     */
    int medPrepDuration;
    /**
     * In minutes
     */
    int treatmentDuration;
}
