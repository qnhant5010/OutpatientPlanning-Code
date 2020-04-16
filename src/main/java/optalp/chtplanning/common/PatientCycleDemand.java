package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class PatientCycleDemand {
    long id;
    /**
     * Unmodifiable
     */
    List<PatientRdvDemand> rdvDemands;

    public PatientCycleDemand(long id, List<PatientRdvDemand> rdvDemands) {
        this.id = id;
        this.rdvDemands = Collections.unmodifiableList(rdvDemands);
    }
}
