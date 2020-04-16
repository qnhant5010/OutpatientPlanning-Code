package instance;

import lombok.Value;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;

import java.util.List;

@Value
public class Instance {
    Param param;
    List<PatientCycleDemand> demands;
}
