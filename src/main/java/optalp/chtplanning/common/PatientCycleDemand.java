package optalp.chtplanning.common;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class PatientCycleDemand {
    @NonNull
    Long id;
    /**
     * Unmodifiable
     */
    @NonNull
    List<PatientRdvDemand> rdvDemands;

    public PatientCycleDemand(@NonNull Long id,
                              @NonNull List<PatientRdvDemand> rdvDemands) {
        this.id = id;
        if (rdvDemands.isEmpty())
            throw new IllegalArgumentException("RDV demands must not be empty");
        this.rdvDemands = Collections.unmodifiableList(rdvDemands);
    }
}
