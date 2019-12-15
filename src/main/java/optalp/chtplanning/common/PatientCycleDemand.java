package optalp.chtplanning.common;

import lombok.Data;

import java.util.List;

@Data
public class PatientCycleDemand {
    private long id;
    /**
     * The first request should have delay equal
     * to 0.
     */
    private List<PatientRdvDemand> rdvDemands;
}
