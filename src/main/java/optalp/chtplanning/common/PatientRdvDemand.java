package optalp.chtplanning.common;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Per patient's demand
 */
@Data
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
    @NonNull
    Boolean needingConsultation;
    @NonNull
    Integer consultationDuration;
    @NonNull
    Integer installationDuration;
    @NonNull
    @SerializedName("medPreparedSameDay")
    Boolean drugMixingSameDay;
    @NonNull
    @SerializedName("medPrepDuration")
    Integer drugMixingDuration;
    @NonNull
    Integer treatmentDuration;

    public Boolean isDrugMixingSameDay() {
        return drugMixingSameDay;
    }
}
