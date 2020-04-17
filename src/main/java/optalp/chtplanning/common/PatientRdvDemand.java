package optalp.chtplanning.common;

import lombok.*;

/**
 * Per patient's demand
 */
@Value
@Builder
public class PatientRdvDemand {
    @NonNull
    Integer id;
    /**
     * 0 means no speciality required
     */
    @NonNull
    Long sectorId;
    /**
     * In days
     */
    @NonNull
    Integer afterLastRequest;
    /**
     * In minutes
     */
    @NonNull
    Integer consultationDuration;
    /**
     * In minutes
     */
    @NonNull
    Integer installationDuration;
    @NonNull
    @Getter(AccessLevel.NONE)
    Boolean drugMixingSameDay;
    /**
     * In minutes
     */
    @NonNull
    Integer drugMixingDuration;
    /**
     * In minutes
     */
    @NonNull
    Integer treatmentDuration;

    public Boolean isDrugMixingSameDay() {
        return drugMixingSameDay;
    }
}
