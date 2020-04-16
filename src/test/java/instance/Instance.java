package instance;

import lombok.NonNull;
import lombok.Value;
import optalp.chtplanning.common.Incompatibility;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;

import java.util.List;
import java.util.Set;

@Value
public class Instance {
    @NonNull
    Param param;
    @NonNull
    List<PatientCycleDemand> demands;
    @NonNull
    Set<Incompatibility> incompatibilities;
}
