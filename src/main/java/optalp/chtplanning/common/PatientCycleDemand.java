package optalp.chtplanning.common;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PatientCycleDemand {
    long id;
    /**
     * The first request should have delay equal
     * to 0.
     */
    List<PatientRdvDemand> rdvDemands;
}
