import lombok.AllArgsConstructor;
import lombok.Data;
import optalp.chtplanning.common.Param;
import optalp.chtplanning.common.PatientCycleDemand;

import java.util.List;

@Data
@AllArgsConstructor
class Instance {
    private Param param;
    private List<PatientCycleDemand> demands;
}
